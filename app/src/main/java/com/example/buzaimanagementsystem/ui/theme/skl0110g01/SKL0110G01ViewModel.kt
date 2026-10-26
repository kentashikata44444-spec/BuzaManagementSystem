package com.example.buzaimanagementsystem.ui.theme.skl0110g01

import android.app.Application
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.buzaimanagementsystem.utils.BluetoothConnectionHelper
import com.example.buzaimanagementsystem.utils.FileLogger
import kotlinx.coroutines.launch

class SKL0110G01ViewModel(
    application: Application,
    private val apiService: SKL0110G01ApiService? = null
) : AndroidViewModel(application) {

    companion object {
        private const val TAG = "SKL0110G01ViewModel"
    }

    var userCd by mutableStateOf("")
        private set
    var gymTntCd by mutableStateOf("")
        private set
    var gymTntMei by mutableStateOf("")
        private set
    var sourceDivision by mutableStateOf("")
        private set
    var seihinBango by mutableStateOf("")
        private set

    // 画面表示用の状態（仕分け: 保管資材確定画面用）
    var hokanShizaiName by mutableStateOf("")
        private set
    var hokanBoxName by mutableStateOf("")
        private set
    var targetHokanBoxCd by mutableStateOf("")
        private set
    var targetHokanShizaiCd by mutableStateOf("")
        private set

    // メッセージエリア
    var shizaiMessage by mutableStateOf("")
        private set
    var boxMessage by mutableStateOf("")
        private set

    var registeredCount by mutableStateOf(0)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set
    var infoMessage by mutableStateOf<String?>(null)
        private set

    var isBleDeviceConnected: Boolean = false
        private set

    var savedBleAddress by mutableStateOf<String?>(null)
        private set

    fun initParams(
        userCd: String,
        gymTntCd: String,
        gymTntMei: String,
        sourceDivision: String,
        seihinBango: String
    ) {
        this.userCd = userCd
        this.gymTntCd = gymTntCd
        this.gymTntMei = gymTntMei
        this.sourceDivision = sourceDivision
        this.seihinBango = seihinBango

        writeLog("I", "initParams完了 -> userCd=$userCd, gymTntCd=$gymTntCd, sourceDivision=$sourceDivision, seihinBango=$seihinBango")

        val context = getApplication<Application>()
        savedBleAddress = BluetoothConnectionHelper.getSavedBleDeviceAddress(context)
        if (!savedBleAddress.isNullOrEmpty()) {
            if (BluetoothConnectionHelper.isBleConnected(context, savedBleAddress)) {
                isBleDeviceConnected = true
                connectBleWithDataListener(context)
            }
        }

        // 前処理：初期データの取得（保管資材確定対象検索）
        fetchHokanData()
    }

    private fun connectBleWithDataListener(context: android.content.Context) {
        viewModelScope.launch {
            BluetoothConnectionHelper.connectToSavedDevice(
                context = context,
                onConnectionStateChanged = { isConnected, address ->
                    isBleDeviceConnected = isConnected
                    writeLog("I", "BLE接続状態変更: isConnected=$isConnected, address=$address")
                },
                onDetailedError = { err ->
                    writeLog("W", "BLE接続エラー: $err")
                },
                onDataReceived = { scannedText ->
                    writeLog("I", "BLEデータ受信(スキャン検知): $scannedText")
                    processScannedCode(scannedText)
                }
            )
        }
    }

    private fun fetchHokanData() {
        writeLog("I", "fetchHokanData 開始: seihinBango=$seihinBango")
        viewModelScope.launch {
            try {
                if (apiService == null) {
                    writeLog("W", "apiServiceがnullのため、モックデータで動作します")
                    registeredCount = 0
                    shizaiMessage = "※『保管BOX』または『保管資材』のQRコードを読み込んでください"
                    return@launch
                }

                val response = apiService.getHokanInfo(seihinBango)
                if (response.isSuccessful && response.body() != null) {
                    val data = response.body()!!
                    if (data.success) {
                        registeredCount = data.count
                        targetHokanBoxCd = data.hokanBoxCd ?: ""
                        hokanBoxName = data.hokanBoxMeisho ?: ""

                        // 初期表示時のガイドメッセージ
                        shizaiMessage = "※『保管BOX』または『保管資材』のQRコードを読み込んでください"

                        writeLog("I", "保管情報取得成功 -> count=${data.count}, boxCd=$targetHokanBoxCd, boxMeisho=$hokanBoxName")
                    } else {
                        errorMessage = data.message ?: "保管資材確定対象データが存在しません"
                        writeLog("W", "保管情報取得失敗: ${data.message}")
                    }
                } else {
                    errorMessage = "保管資材確定対象データが存在しません"
                    writeLog("W", "保管情報取得APIレスポンスエラー: code=${response.code()}")
                }
            } catch (e: Exception) {
                writeLog("E", "保管情報取得中に例外発生: ${e.message}", e)
                errorMessage = "システム管理者に連絡してください"
            }
        }
    }

    /**
     * メイン処理：バーコード（QR）が読み込まれた場合
     * 仕様：保管種類（HSIZAI または HBOX）のチェックとバリデーション[cite: 6]
     */
    fun processScannedCode(scannedText: String) {
        writeLog("I", "processScannedCode 開始: scannedText=$scannedText")
        errorMessage = null
        infoMessage = null
        shizaiMessage = ""
        boxMessage = ""

        val tokens = scannedText.split("|").map { it.trim() }
        if (tokens.isEmpty()) {
            errorMessage = "保管BOXまたは、保管資材以外が読み込まれました"
            writeLog("W", "スキャンエラー: トークンが空です")
            return
        }

        val hokanShurui = tokens[0]
        writeLog("I", "読み込まれた保管種類: $hokanShurui")

        // 仕様：保管種類が "HSIZAI" 又は "HBOX" 以外のチェック[cite: 6]
        if (hokanShurui != "HSIZAI" && hokanShurui != "HBOX") {
            errorMessage = "保管BOXまたは、保管資材以外が読み込まれました"
            writeLog("W", "保管種類エラー: 想定外のQRが読み込まれました ($hokanShurui)")
            return
        }

        if (tokens.size < 2) {
            errorMessage = "コードが設定されていません"
            writeLog("W", "スキャンエラー: コードが含まれていません")
            return
        }

        val scannedCode = tokens[1]
        writeLog("I", "取得したコード: $scannedCode")

        if (hokanShurui == "HSIZAI") {
            validateAndRegisterShizai(scannedCode, tokens)
        } else {
            validateAndRegisterBox(scannedCode)
        }
    }

    private fun validateAndRegisterShizai(shizaiCd: String, tokens: List<String>) {
        writeLog("I", "validateAndRegisterShizai 開始: shizaiCd=$shizaiCd")
        viewModelScope.launch {
            try {
                if (apiService == null) {
                    errorMessage = "APIサービスが初期化されていません"
                    return@launch
                }

                // フォーマット（HSIZAI|PL08|PALLET08）の2番目（tokens[1]）を中分類コードとして取得
                val chubunruiCd = if (tokens.size > 1) tokens[1] else shizaiCd
                writeLog("I", "中分類マスタ検索実行: chubunruiCd=$chubunruiCd")

                val chuResponse = apiService.getChuMaster(chubunruiCd)
                if (!chuResponse.isSuccessful || chuResponse.body() == null || !chuResponse.body()!!.success) {
                    errorMessage = "読み込まれた保管資材がマスタに存在しません\n確認してください"
                    writeLog("W", "保管資材マスタ検索失敗: code=$chubunruiCd")
                    return@launch
                }

                val chubunruiMeisho = chuResponse.body()!!.chubunruiMeisho ?: shizaiCd
                hokanShizaiName = chubunruiMeisho
                targetHokanShizaiCd = shizaiCd
                infoMessage = "保管資材を設定しました"
                writeLog("I", "保管資材設定成功: 名称=$chubunruiMeisho")

            } catch (e: Exception) {
                writeLog("E", "保管資材検証中に例外発生: ${e.message}", e)
                errorMessage = "システム管理者に連絡してください"
            }
        }
    }

    private fun validateAndRegisterBox(boxCd: String) {
        writeLog("I", "validateAndRegisterBox 開始: boxCd=$boxCd")
        viewModelScope.launch {
            try {
                if (apiService == null) {
                    errorMessage = "APIサービスが初期化されていません"
                    return@launch
                }

                val areaResponse = apiService.getBoxInfo(boxCd)
                if (!areaResponse.isSuccessful || areaResponse.body() == null || !areaResponse.body()!!.success) {
                    errorMessage = "読み込まれた保管BOXがマスタに存在しません\n確認してください"
                    writeLog("W", "保管BOXマスタ検索失敗: code=$boxCd")
                    return@launch
                }

                val boxMeisho = areaResponse.body()!!.hokanBoxMeisho ?: boxCd
                hokanBoxName = boxMeisho
                targetHokanBoxCd = boxCd
                infoMessage = "保管BOXを設定しました"
                writeLog("I", "保管BOX設定成功: 名称=$boxMeisho")

            } catch (e: Exception) {
                writeLog("E", "保管BOX検証中に例外発生: ${e.message}", e)
                errorMessage = "システム管理者に連絡してください"
            }
        }
    }

    private fun writeLog(level: String, message: String, throwable: Throwable? = null) {
        val context = getApplication<Application>()
        when (level) {
            "D" -> Log.d(TAG, message, throwable)
            "W" -> Log.w(TAG, message, throwable)
            "E" -> Log.e(TAG, message, throwable)
            "I" -> Log.i(TAG, message, throwable)
        }
        FileLogger.writeLog(context, level, TAG, message)
    }
}