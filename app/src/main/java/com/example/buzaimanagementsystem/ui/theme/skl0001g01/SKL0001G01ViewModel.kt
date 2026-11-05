package com.example.buzaimanagementsystem.ui.theme.skl0001g01

import android.app.Application
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.buzaimanagementsystem.utils.AppConfig
import com.example.buzaimanagementsystem.utils.BluetoothConnectionHelper
import com.example.buzaimanagementsystem.utils.FileLogger
import kotlinx.coroutines.launch
import java.net.NetworkInterface
import java.util.Calendar
import java.util.Collections

class SKL0001G01ViewModel(
    application: Application,
    private val apiService: SKL0001G01ApiService
) : AndroidViewModel(application) {

    companion object {
        private const val TAG = "SKL0001G01ViewModel"
    }

    var displayTime by mutableStateOf("00:00～00:00")
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    var isSystemAvailable by mutableStateOf(true)
        private set

    var loggedInUserCd by mutableStateOf("")
        private set

    var isLoggingIn by mutableStateOf(false)
        private set

    // 画面に即時反映するユーザーID保持用ステート
    var inputUserCd by mutableStateOf("")
        private set

    private var loginSuccessCallback: ((String, String, String, String) -> Unit)? = null

    fun setLoginSuccessCallback(callback: (String, String, String, String) -> Unit) {
        loginSuccessCallback = callback
    }

    private fun writeLog(level: String, message: String, throwable: Throwable? = null) {
        val context = getApplication<Application>()
        when (level) {
            "D" -> {
                Log.d(TAG, message, throwable)
                FileLogger.writeLog(context, "DEBUG", TAG, message + (throwable?.let { " : ${it.message}" } ?: ""))
            }
            "W" -> {
                Log.w(TAG, message, throwable)
                FileLogger.writeLog(context, "WARN", TAG, message + (throwable?.let { " : ${it.message}" } ?: ""))
            }
            "E" -> {
                Log.e(TAG, message, throwable)
                FileLogger.writeLog(context, "ERROR", TAG, message + (throwable?.let { " : ${it.message}" } ?: ""))
            }
            "I" -> {
                Log.i(TAG, message, throwable)
                FileLogger.writeLog(context, "INFO", TAG, message + (throwable?.let { " : ${it.message}" } ?: ""))
            }
        }
    }

    fun disconnectBleDevice() {
        val context = getApplication<Application>()
        writeLog("I", "disconnectBleDevice 呼び出し: BLE切断処理を実行します")
        BluetoothConnectionHelper.disconnectDevice(context)
    }

    override fun onCleared() {
        super.onCleared()
        disconnectBleDevice()
    }

    private fun startBleConnection(context: android.content.Context) {
        BluetoothConnectionHelper.connectToSavedDevice(
            context = context,
            onConnectionStateChanged = { isConnected, address ->
                writeLog("I", "接続コールバック: isConnected=$isConnected, address=$address")
            },
            onDetailedError = { detailedErr ->
                writeLog("W", "BLE接続エラー: $detailedErr")
                errorMessage = detailedErr
            },
            onDataReceived = { scannedText ->
                writeLog("I", "BLEデータ受信(ログインスキャン): $scannedText")
                if (isSystemAvailable && !isLoggingIn && scannedText.isNotBlank()) {
                    executeLogin(scannedText) { uCd, userName, gymTntCd, gymTntMei ->
                        loginSuccessCallback?.invoke(uCd, userName, gymTntCd, gymTntMei)
                    }
                }
            }
        )
    }

    // ▼ 自端末のローカルIPv4アドレスを取得するヘルパー関数
    private fun getLocalIpAddress(): String? {
        try {
            val interfaces = Collections.list(NetworkInterface.getNetworkInterfaces())
            for (networkInterface in interfaces) {
                val addrs = Collections.list(networkInterface.inetAddresses)
                for (addr in addrs) {
                    if (!addr.isLoopbackAddress) {
                        val sAddr = addr.hostAddress ?: continue
                        val isIPv4 = sAddr.indexOf(':') < 0
                        if (isIPv4) {
                            return sAddr
                        }
                    }
                }
            }
        } catch (e: Exception) {
            writeLog("E", "自端末のIPアドレス取得中に例外発生", e)
        }
        return null
    }

    fun checkSystemAvailability() {
        writeLog("D", "checkSystemAvailability: システム稼働状況およびバーコードリーダーの接続を確認します")

        val context = getApplication<Application>()
        val savedAddress = AppConfig.getSavedBleDeviceAddress(context)
        val savedMeisho = AppConfig.getConfiguredDeviceMeisho(context)

        // ==========================================
        // ① 2回目以降：メモリ（SharedPreferences）から読み込んで接続する場合
        // ==========================================
        if (!savedAddress.isNullOrBlank()) {
            writeLog("I", "【保存済み接続】ローカル設定からリーダー情報をロードしました: 銘称=[$savedMeisho], MAC=($savedAddress)。このアドレスで接続します。")
            startBleConnection(context)
        }
        // ==========================================
        // ② 初回起動時：未保存のため、サーバー経由で Reader.txt からIP突合して特定・保存する場合
        // ==========================================
        else {
            writeLog("I", "【初回セットアップ】保存済みMACアドレスが見つかりません。サーバーからReader.txtキャッシュを取得して自機IPとの突合を開始します。")

            viewModelScope.launch {
                try {
                    val readerList = apiService.getBarcodeReaderList()
                    if (readerList.isNotEmpty()) {
                        // 1. 自端末のIPアドレスを取得する
                        val myIpAddress = getLocalIpAddress()?.trim()
                        writeLog("I", "【初回セットアップ】自端末のIPアドレス: $myIpAddress (取得総数: ${readerList.size}件)")

                        var matchedReader: DeviceMappingDto? = null

                        // 2. Reader.txtのキャッシュリストをループさせて、1番目のカラム（端末IP）と自端末のIPが一致する行を探す
                        for ((index, reader) in readerList.withIndex()) {
                            val readerIp = reader.targetIpAddress?.trim() ?: ""
                            writeLog("D", "  - 比較中 [インデックス $index]: 登録IP=[$readerIp], 登録MAC=[${reader.targetMacAddress}], 銘称=[${reader.targetMeisho}]")

                            if (!myIpAddress.isNullOrBlank() && readerIp.equals(myIpAddress, ignoreCase = true)) {
                                matchedReader = reader
                                writeLog("I", "【初回セットアップ】Reader.txt内で自端末のIP($myIpAddress)に一致するリーダー設定を発見しました: 銘称=${reader.targetMeisho}, MAC=${reader.targetMacAddress}")
                                break // 一致したらループを抜ける
                            }
                        }

                        // 3. ループ終了後の判定（IP一致 ＋ MACアドレスの妥当性チェック）
                        if (matchedReader != null) {
                            val rawMac = matchedReader.targetMacAddress?.trim()

                            // ★ IPは一致したが、MACアドレスが空、あるいは形式として不正な場合のエラー処理
                            if (rawMac.isNullOrBlank() || rawMac.length < 12) {
                                errorMessage = "バーコードリーダーのMACアドレス設定が不正です\nシステム管理者に問合せ願います"
                                writeLog("W", "【初回セットアップ】自端末のIP(${myIpAddress})に一致する行が見つかりましたが、MACアドレスが空または不正です (MAC=[$rawMac])")
                                return@launch
                            }

                            val formattedMac = if (!rawMac.contains(":") && rawMac.length == 12) {
                                rawMac.chunked(2).joinToString(":")
                            } else {
                                rawMac
                            }.uppercase()

                            val targetName = matchedReader.targetMeisho ?: ""

                            // ★ IPとMACの両方が正常に照合・検証された段階で、次回以降のためにAppConfigへ保存
                            AppConfig.saveBleDeviceAddress(context, formattedMac, targetName)
                            writeLog("I", "【初回セットアップ】IPおよびMACアドレスの照合に成功しました。リーダー情報をAppConfigへ永続保存します: 銘称=$targetName, MAC=$formattedMac")

                            startBleConnection(context)

                        } else {
                            errorMessage = "バーコードリーダーに接続できません\nシステム管理者に問合せ願います"
                            writeLog("W", "【初回セットアップ】Reader.txt全体を照合しましたが、自端末のIP($myIpAddress)に一致する設定行がありませんでした")
                        }
                    } else {
                        errorMessage = "Reader.txtからバーコードリーダーのリストを取得できませんでした"
                        writeLog("W", "【初回セットアップ】APIから取得したReader.txtのリストが空です")
                    }
                } catch (e: Exception) {
                    writeLog("E", "【初回セットアップ】デバイスマッピングの取得中に例外発生", e)
                    errorMessage = "NAS接続エラー: サーバーまたはマッピングAPIを確認してください"
                }
            }
        }

        // （以降の稼働時間・フラグチェックのコードはそのまま継続）
        viewModelScope.launch {
            try {
                val response = apiService.getConditions()

                val timeData = response.operationTime
                if (timeData == null || timeData.suji1.isNullOrBlank() || timeData.suji2.isNullOrBlank()) {
                    isSystemAvailable = false
                    errorMessage = "稼働時間の取得に失敗しました \nシステム管理者に問合せ願います"
                    return@launch
                }

                fun parseHour(value: Any?): Int? {
                    return when (value) {
                        is Number -> value.toInt()
                        is String -> value.toIntOrNull()
                        else -> value?.toString()?.toIntOrNull()
                    }
                }

                val startHour = parseHour(timeData.suji1)
                val endHour = parseHour(timeData.suji2)

                if (startHour != null && endHour != null) {
                    displayTime = "%02d:00～%02d:00".format(startHour, endHour)

                    val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
                    val allowed = if (startHour <= endHour) {
                        currentHour in startHour until endHour
                    } else {
                        currentHour >= startHour || currentHour < endHour
                    }

                    if (!allowed) {
                        isSystemAvailable = false
                        errorMessage = "稼働時間外の為、利用できません"
                        return@launch
                    }
                } else {
                    isSystemAvailable = false
                    errorMessage = "稼働時間の解析に失敗しました \nシステム管理者に問合せ願います"
                    return@launch
                }

                if (response.operationFlag.isNullOrBlank()) {
                    isSystemAvailable = false
                    errorMessage = "稼働フラグ取得に失敗しました \nシステム管理者に問合せ願います"
                    return@launch
                }

                if (!response.operationFlag.trim().equals("OK", ignoreCase = true)) {
                    isSystemAvailable = false
                    errorMessage = "非稼働日の為、利用できません"
                    return@launch
                }

            } catch (e: Exception) {
                isSystemAvailable = false
                errorMessage = "ＤＢ接続に失敗しました \nシステム管理者に問合せ願います"
            }
        }
    }

    fun executeLogin(inputText: String, onSuccess: (userCd: String, userName: String, gymTntCd: String, gymTntMei: String) -> Unit) {
        if (isLoggingIn) return
        if (!isSystemAvailable) {
            if (errorMessage.isNullOrBlank()) errorMessage = "現在システムはご利用いただけません。"
            return
        }

        // 1. ユーザーIDのベース部（アンダーバー手前）を抽出
        val basePart = inputText.split("_", limit = 2)[0]

        // 2. 【条件判断】半角スペース、全角スペース、タブ、英数字・アンダーバー以外の不正文字チェック
        val hasSpaceOrInvalid = basePart.any { char ->
            val code = char.code
            char == ' ' ||
                    char == ' ' ||
                    code == 0x3000 || // 全角スペース
                    code == 0x0020 || // 半角スペース
                    code == 0x00A0 || // NBSP
                    code == 0x0009 || // タブ
                    !(char in 'a'..'z' || char in 'A'..'Z' || char in '0'..'9' || char == '_')
        }

        // 3. 画面のID欄には画面表示用の綺麗な文字列をセット
        inputUserCd = basePart.filter { char ->
            char in 'a'..'z' || char in 'A'..'Z' || char in '0'..'9' || char == '_'
        }

        // 4. スペースや不正文字が含まれている場合はエラーとして弾く
        if (hasSpaceOrInvalid || basePart.isBlank()) {
            errorMessage = "ユーザーが登録されていません \nユーザー登録してください"
            isLoggingIn = false
            writeLog("W", "executeLogin: スペースまたは不正文字が検出されたため拒否しました (inputText=$inputText)")
            return
        }

        // 5. 条件をクリアした正常な文字列でログイン処理へ進む
        val inputUser = basePart.trim()
        isLoggingIn = true
        errorMessage = null

        viewModelScope.launch {
            try {
                val request = SKL0001G01LoginRequest(
                    userCd = inputUser,
                    password = "",
                    userName = "",
                    bushoCd = "",
                    bushoName = ""
                )

                val response = apiService.login(request)
                if (response.success) {
                    val name = response.data?.userName?.takeIf { it.isNotBlank() } ?: inputUser
                    val tntCd = response.data?.gymTntCd ?: ""
                    val tntMei = response.data?.gymTntMei ?: ""
                    val bushoCd = response.data?.bushoCd ?: ""
                    val bushoName = response.data?.bushoName ?: ""

                    // ★ 部署マスタの登録チェック
                    if (bushoCd.isBlank() || bushoName.isBlank()) {
                        errorMessage = "部署マスタが登録されていません \nシステム管理者に問合せ願います"
                        isLoggingIn = false
                        writeLog("W", "executeLogin: 部署マスタのコードまたは名称が空です (bushoCd=$bushoCd, bushoName=$bushoName)")
                        return@launch
                    }

                    // ★ 業務担当マスタの登録チェック
                    if (tntCd.isBlank() || tntMei.isBlank()) {
                        errorMessage = "業務担当マスタが登録されていません \nシステム管理者に問合せ願います"
                        isLoggingIn = false
                        writeLog("W", "executeLogin: 業務担当マスタのコードまたは名称が空です (gymTntCd=$tntCd, gymTntMei=$tntMei)")
                        return@launch
                    }

                    loggedInUserCd = inputUser
                    onSuccess(inputUser, name, tntCd, tntMei)
                } else {
                    errorMessage = response.message ?: "ユーザーが登録されていません \nユーザー登録してください"
                    isLoggingIn = false
                }
            } catch (e: Exception) {
                writeLog("E", "executeLogin: 通信エラー", e)
                errorMessage = "通信エラー、またはシステムエラーが発生しました。"
                isLoggingIn = false
            }
        }
    }
}