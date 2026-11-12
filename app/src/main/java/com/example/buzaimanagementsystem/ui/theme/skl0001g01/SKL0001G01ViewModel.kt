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
import java.util.Calendar

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

    fun checkSystemAvailability() {
        writeLog("D", "checkSystemAvailability: システム稼働状況およびバーコードリーダーの接続を確認します")

        val context = getApplication<Application>()

        // ★ 各端末の server_config.txt (またはローカル設定) から直接リーダーのMACアドレスを取得
        val savedAddress = AppConfig.getSavedBleDeviceAddress(context)
        val savedMeisho = AppConfig.getConfiguredDeviceMeisho(context)

        if (!savedAddress.isNullOrBlank()) {
            writeLog("I", "【BLE接続】ローカル設定からリーダー情報をロードしました: 銘称=[$savedMeisho], MAC=($savedAddress)。このアドレスで接続します。")
            startBleConnection(context)
        } else {
            errorMessage = "バーコードリーダーのMACアドレスが設定されていません\nserver_config.txtを確認してください"
            writeLog("W", "【BLE接続】server_config.txt に有効なMACアドレスが保存されていません")
        }

        // 稼働時間・フラグチェックの処理
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
                    char == ' ' || // 全角スペース
                    code == 0x3000 ||
                    code == 0x0020 ||
                    code == 0x00A0 ||
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