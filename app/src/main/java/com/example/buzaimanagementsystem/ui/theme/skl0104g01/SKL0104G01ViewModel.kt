package com.example.buzaimanagementsystem.ui.theme.skl0104g01

import android.app.Application
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.buzaimanagementsystem.utils.BluetoothConnectionHelper
import com.example.buzaimanagementsystem.utils.FileLogger
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SKL0104G01ViewModel(
    application: Application,
    private val apiService: SKL0104G01ApiService
) : AndroidViewModel(application) {

    companion object {
        private const val TAG = "SKL0104G01ViewModel"
    }

    var userCd by mutableStateOf("")
        private set
    var gymTntCd by mutableStateOf("")
        private set
    var gymTntMei by mutableStateOf("")
        private set
    var sourceDivision by mutableStateOf("1")
        private set

    // 入力・スキャン項目
    var hokanBoxCode by mutableStateOf("")
        private set
    var hokanShizaiCode by mutableStateOf("")
        private set

    var chubunruiCode by mutableStateOf("")
        private set
    var chubunruiMeisho by mutableStateOf("")
        private set
    var shobunruiCode by mutableStateOf("")
        private set
    var shobunruiMeisho by mutableStateOf("")
        private set

    var seihinBango by mutableStateOf("")
        private set
    var denpyoBango by mutableStateOf("")
        private set
    var shikyuYoteiSu by mutableStateOf("")
        private set
    var konkaiShikyuSu by mutableStateOf("")
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set
    var infoMessage by mutableStateOf<String?>(null)
        private set

    // BLEバーコードリーダーの接続状態を保持するフラグ
    var isBleDeviceConnected: Boolean = false
        private set

    // 現在BLE接続処理（非同期）の真っ最中であるかを保持するフラグ
    var isBleConnecting: Boolean = false
        private set

    // 前回保存されたBLEデバイスのアドレスを保持する変数
    var savedBleAddress by mutableStateOf<String?>(null)
        private set

    /**
     * 外部（BLE管理クラスやコールバック）から接続状態を変更されたときに呼ぶメソッド
     */
    fun updateBleConnectionStatus(isConnected: Boolean, deviceAddress: String? = null) {
        isBleDeviceConnected = isConnected
        isBleConnecting = false // 接続試行が完了したのでフラグを降ろす
        writeLog("I", "BLE接続状態が更新されました: isConnected=$isConnected, address=$deviceAddress")

        val context = getApplication<Application>()

        // 接続に成功し、デバイスアドレスが取得できている場合はAppConfigに保存する
        if (isConnected && !deviceAddress.isNullOrEmpty()) {
            BluetoothConnectionHelper.saveBleDeviceAddress(context, deviceAddress)
            savedBleAddress = deviceAddress
            writeLog("I", "BLEデバイスのアドレスをConfigに保存しました: $deviceAddress")
        }

        // 状態が変わったタイミングで再度接続チェックを走らせてメッセージを更新する
        checkBarcodeReaderConnection(context)
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

    fun initParams(
        userCd: String,
        gymTntCd: String,
        gymTntMei: String,
        sourceDivision: String,
        chuCode: String = "",
        chuName: String = "",
        shoCode: String = "",
        shoName: String = ""
    ) {
        writeLog("D", "initParams: パラメータ初期化 (userCd=$userCd, gymTntCd=$gymTntCd, gymTntMei=$gymTntMei, sourceDivision=$sourceDivision)")
        this.userCd = userCd
        this.gymTntCd = gymTntCd
        this.gymTntMei = gymTntMei
        this.sourceDivision = sourceDivision

        if (chuCode.isNotEmpty()) hokanShizaiCode = chuCode
        if (chuName.isNotEmpty()) chubunruiMeisho = chuName
        if (shoCode.isNotEmpty()) hokanBoxCode = shoCode
        if (shoName.isNotEmpty()) shobunruiMeisho = shoName

        val context = getApplication<Application>()

        // 1. 保存済みのアドレスを変数に保持
        savedBleAddress = BluetoothConnectionHelper.getSavedBleDeviceAddress(context)
        if (!savedBleAddress.isNullOrEmpty()) {
            writeLog("I", "Configから保存済みBLEアドレスを読み込みました: $savedBleAddress")

            // 2. すでにOS側で接続されている場合は接続処理をスルーする
            if (BluetoothConnectionHelper.isBleConnected(context, savedBleAddress)) {
                writeLog("I", "すでにBLEが接続されているため、接続試行をスキップします")
                isBleDeviceConnected = true
                checkBarcodeReaderConnection(context)
                return
            }

            // 3. 未接続の場合は、自動接続を1回だけ試みる
            isBleConnecting = true
            errorMessage = null // 接続開始時はエラーをクリア
            infoMessage = "BLEバーコードリーダーに接続中..."

            writeLog("I", "initParams: 500ms後にBLE自動接続コルーチンを開始します")

            // 少し待機時間（500ms）を設けてから接続を試みる
            viewModelScope.launch {
                delay(500)
                writeLog("I", "initParams: 500ms遅延完了。BluetoothConnectionHelper.connectToSavedDevice を呼び出します")

                // 待機中にすでに接続が確立された場合はスルーする
                if (BluetoothConnectionHelper.isBleConnected(context, savedBleAddress)) {
                    writeLog("I", "initParams: 待機中にすでにBLE接続が確立されていました")
                    isBleConnecting = false
                    isBleDeviceConnected = true
                    checkBarcodeReaderConnection(context)
                    return@launch
                }

                BluetoothConnectionHelper.connectToSavedDevice(context) { isConnected, address ->
                    writeLog("I", "BLE接続コールバックを受信しました: isConnected=$isConnected, address=$address")
                    updateBleConnectionStatus(isConnected, address)
                }
            }
        } else {
            writeLog("I", "Configに保存済みBLEアドレスが存在しません")
            // 保存アドレスがない場合はそのまま通常のチェックを行う
            checkBarcodeReaderConnection(context)
        }
    }

    /**
     * バーコードリーダー（BLE）およびBluetooth自体の接続状態をチェックする
     */
    fun checkBarcodeReaderConnection(context: android.content.Context) {
        if (!BluetoothConnectionHelper.isBluetoothEnabled()) {
            errorMessage = "エラー: BluetoothがOFFになっています。ONにしてください。"
            infoMessage = null // 片方を出す時は片方をクリア
            writeLog("E", "BluetoothがOFFです")
            return
        }

        // 現在接続処理中であれば「接続中...」を表示し、エラーはクリアする
        if (isBleConnecting) {
            errorMessage = null
            infoMessage = "BLEバーコードリーダーに接続中..."
            writeLog("I", "現在BLE接続処理中です...")
            return
        }

        // OS側の実際の接続状態を確認してフラグを同期
        val isConnected = BluetoothConnectionHelper.isBleConnected(context, savedBleAddress)
        isBleDeviceConnected = isConnected

        if (!isConnected) {
            if (!savedBleAddress.isNullOrEmpty()) {
                writeLog("I", "前回接続されたBLEアドレス ($savedBleAddress) がConfigに存在します")
            }
            errorMessage = "警告: BLEバーコードリーダーが接続されていません"
            infoMessage = null // 未接続時はインフォメーションを消す
            writeLog("W", "BLEバーコードリーダーが未接続です")
        } else {
            errorMessage = null // 接続成功時はエラーを確実にクリア
            infoMessage = "BLEバーコードリーダー接続OK"
            writeLog("I", "BLEバーコードリーダー接続OK")
        }
    }

    fun clearInput() {
        hokanBoxCode = ""
        hokanShizaiCode = ""
        chubunruiCode = ""
        chubunruiMeisho = ""
        shobunruiCode = ""
        shobunruiMeisho = ""
        seihinBango = ""
        denpyoBango = ""
        shikyuYoteiSu = ""
        konkaiShikyuSu = ""
        errorMessage = null
        infoMessage = null
    }

    fun processScannedCode(scannedText: String) {
        val tokens = scannedText.split("|")
        if (tokens.isEmpty()) {
            errorMessage = "※『保管BOX』または『保管資材』のQRコードを読み込んでください"
            return
        }
    }

    fun executeRegister() {
        infoMessage = "登録処理が完了しました"
    }

    fun executeDelete() {
        infoMessage = "削除処理が完了しました"
    }

    fun setInitialSelection(chuCode: String, chuName: String, shoCode: String, shoName: String) {
        chubunruiCode = chuCode
        chubunruiMeisho = chuName
        shobunruiCode = shoCode
        shobunruiMeisho = shoName
        if (chuCode.isNotEmpty()) hokanShizaiCode = chuCode
        if (shoCode.isNotEmpty()) hokanBoxCode = shoCode
    }
}