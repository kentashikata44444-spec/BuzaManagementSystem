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

    // API送信用の4つの変数
    var hokanBoxCode by mutableStateOf("")
        private set
    var hokanBoxMeisho by mutableStateOf("")
        private set
    var hokanShizaiCode by mutableStateOf("")
        private set
    var hokanShizaiMeisho by mutableStateOf("")
        private set

    // 画面側（SKL0104G01Screen）が参照している分類変数（UIとの同期用）
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
    var buhinKanriNo by mutableStateOf("")
        private set
    var hinban by mutableStateOf("")
        private set

    // スキャンされたQRコードの種類に基づく伝票種別コードを保持（デフォルトは "03"）
    var scannedDenpyoShubetsu by mutableStateOf("03")
        private set

    var shikyuYoteiSu by mutableStateOf("0")
        private set
    var konkaiShikyuSu by mutableStateOf("0")
        private set

    var tehaiSu by mutableStateOf(0.0)
        private set

    var isEmergency by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set
    var infoMessage by mutableStateOf<String?>(null)
        private set

    var isBleDeviceConnected: Boolean = false
        private set

    var savedBleAddress by mutableStateOf<String?>(null)
        private set

    // ==========================================
    // ★ ボタン制御用の状態・判定プロパティ
    // ==========================================
    var registeredItemCount by mutableStateOf(0)
        private set

    var isDataLoadedForRegister by mutableStateOf(false)
        private set

    // 保管BOX・保管資材変更: 常に有効
    val isChangeBoxOrMaterialEnabled: Boolean = true

    // 保管資材説明: 常に有効
    val isMaterialDescriptionEnabled: Boolean = true

    // 一時保管（保管エリア確定）: 登録件数が1件以上の場合は有効、ゼロ件は無効
    val isTemporaryStorageEnabled: Boolean
        get() = registeredItemCount > 0

    // 緊急品集約: 緊急品表示が有りの場合は有効、無しの場合は無効
    val isEmergencyButtonEnabled: Boolean
        get() = isEmergency

    // 削除: データが読み込まれている場合は有効
    val isDeleteEnabled: Boolean
        get() = seihinBango.isNotBlank() || denpyoBango.isNotBlank()

    // 登録: データが正常に読み込まれ登録待ち状態の時は有効
    val isRegisterEnabled: Boolean
        get() = isDataLoadedForRegister

    // 仕分け・一時保管メニュー: 登録件数がゼロ件の場合は有効、1件以上の場合は無効
    val isMenuEnabled: Boolean
        get() = registeredItemCount == 0

    // 読み取り: 常に有効
    val isScanEnabled: Boolean = true
    // ==========================================

    fun updateBleConnectionStatus(isConnected: Boolean, deviceAddress: String? = null) {
        isBleDeviceConnected = isConnected
        writeLog("I", "BLE接続状態が更新されました: isConnected=$isConnected, address=$deviceAddress")

        val context = getApplication<Application>()

        if (isConnected && !deviceAddress.isNullOrEmpty()) {
            BluetoothConnectionHelper.saveBleDeviceAddress(context, deviceAddress)
            savedBleAddress = deviceAddress
            errorMessage = null
            infoMessage = null
        } else {
            isBleDeviceConnected = false
            errorMessage = "Bluetoothがオフです\nBluetoothをオンにしてください"
            writeLog("W", "BLEデバイスが切断されました / Bluetoothオフ")
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
        this.userCd = userCd
        this.gymTntCd = gymTntCd
        this.gymTntMei = gymTntMei
        this.sourceDivision = sourceDivision

        if (chuCode.isNotEmpty() || shoCode.isNotEmpty() || (hokanShizaiCode.isEmpty() && hokanBoxCode.isEmpty())) {
            setInitialSelection(chuCode, chuName, shoCode, shoName)
            writeLog("I", "initParams完了 -> ChuCode: '$chuCode', ChuName: '$chuName', ShoCode: '$shoCode', ShoName: '$shoName'")
        } else {
            writeLog("I", "initParamsスキップ（既存の選択値を保持） -> 現在の資材: '$hokanShizaiCode', BOX: '$hokanBoxCode'")
        }

        val context = getApplication<Application>()
        savedBleAddress = BluetoothConnectionHelper.getSavedBleDeviceAddress(context)
        if (savedBleAddress.isNullOrEmpty()) {
            errorMessage = "ハードウェア異常 システム管理者に問合せ願います"
            return
        }

        if (BluetoothConnectionHelper.isBleConnected(context, savedBleAddress)) {
            isBleDeviceConnected = true
            startConnectionMonitor(context)
            connectBleWithDataListener(context)
            return
        }

        connectBleWithDataListener(context)
    }

    private fun connectBleWithDataListener(context: android.content.Context) {
        viewModelScope.launch {
            BluetoothConnectionHelper.connectToSavedDevice(
                context = context,
                onConnectionStateChanged = { isConnected, address ->
                    updateBleConnectionStatus(isConnected, address)
                    if (isConnected) startConnectionMonitor(context)
                },
                onDetailedError = { _ ->
                    errorMessage = "ハードウェア異常 システム管理者に問合せ願います"
                    isBleDeviceConnected = false
                },
                onDataReceived = { scannedText ->
                    writeLog("I", "BLEデータ受信コールバック発火: $scannedText")
                    processScannedCode(scannedText)
                }
            )
        }
    }

    private var monitorJob: kotlinx.coroutines.Job? = null

    private fun startConnectionMonitor(context: android.content.Context) {
        if (monitorJob?.isActive == true) return
        monitorJob = viewModelScope.launch {
            while (true) {
                kotlinx.coroutines.delay(3000L)
                val currentConnected = BluetoothConnectionHelper.isBleConnected(context, savedBleAddress)
                if (isBleDeviceConnected && !currentConnected) {
                    updateBleConnectionStatus(false, savedBleAddress)
                    break
                }
            }
        }
    }

    fun checkBarcodeReaderConnection(context: android.content.Context) {
        if (!BluetoothConnectionHelper.isBluetoothEnabled()) {
            errorMessage = "Bluetoothがオフです\nBluetoothをオンにしてください"
            return
        }
        val isConnected = BluetoothConnectionHelper.isBleConnected(context, savedBleAddress)
        isBleDeviceConnected = isConnected
        if (!isConnected) {
            errorMessage = "バーコードリーダーの電源がオフです\nバーコードリーダーの電源をいれてください"
        }
    }

    fun clearInput() {
        writeLog("I", "clearInput実行: 入力状態をクリアします (現在の登録件数: $registeredItemCount)")
        seihinBango = ""
        denpyoBango = ""
        buhinKanriNo = ""
        hinban = ""
        scannedDenpyoShubetsu = "03"
        shikyuYoteiSu = "0"
        konkaiShikyuSu = "0"
        tehaiSu = 0.0
        isEmergency = false
        isDataLoadedForRegister = false
        errorMessage = null
        infoMessage = null
        writeLog("I", "clearInput完了 -> isRegisterEnabled: $isRegisterEnabled, isDeleteEnabled: $isDeleteEnabled")
    }

    fun processScannedCode(scannedText: String) {
        if (scannedText.isBlank()) {
            errorMessage = "※『保管BOX』または『保管資材』のQRコードを読み込んでください"
            writeLog("W", "processScannedCode: 空のテキストがスキャンされました")
            return
        }

        errorMessage = null
        infoMessage = null
        isDataLoadedForRegister = false

        if (!scannedText.contains("|")) {
            errorMessage = "データがTECHS部品表情報に存在しません"
            writeLog("W", "スキャン解析エラー: パイプ区切りが含まれていません -> $scannedText")
            clearInputFields()
            return
        }

        val tokens = scannedText.split("|").map { it.trim() }
        writeLog("I", "スキャンデータ解析: 項目数=${tokens.size}, テキスト=$scannedText")

        var targetSeihinBango = ""
        var targetBuhinKanriNo = ""
        var targetHinban = ""

        fun formatNumber(valueStr: String): String? {
            val doubleVal = valueStr.toDoubleOrNull() ?: return null
            return if (doubleVal % 1.0 == 0.0) {
                doubleVal.toInt().toString()
            } else {
                String.format(java.util.Locale.US, "%.2f", doubleVal).trimEnd('0').trimEnd('.')
            }
        }

        when {
            tokens.size == 3 -> {
                val qtyStr = tokens[2]
                val formattedQty = formatNumber(qtyStr)
                if (formattedQty == null) {
                    errorMessage = "データがTECHS部品表情報に存在しません"
                    writeLog("W", "スキャン解析エラー(3項目): 数量が不正です -> $qtyStr")
                    clearInputFields()
                    return
                }

                buhinKanriNo = tokens[1]
                targetBuhinKanriNo = tokens[1]
                shikyuYoteiSu = formattedQty
                konkaiShikyuSu = formattedQty
                scannedDenpyoShubetsu = "04"
                writeLog("I", "種別判定: 部材現品票検出 -> denpyoShubetsuCd='04', buhinKanriNo='$buhinKanriNo', qty='$formattedQty'")
            }
            tokens.size >= 2 && tokens[1] == "03" -> {
                val orderNo = tokens[0]
                denpyoBango = orderNo

                targetSeihinBango = if (tokens.size > 5) tokens[5] else ""
                targetHinban = if (tokens.size > 6) tokens[6] else ""

                seihinBango = targetSeihinBango
                hinban = targetHinban

                if (tokens.size >= 4) {
                    val chuzanStr = tokens[3]
                    val formattedChuzan = formatNumber(chuzanStr)
                    if (formattedChuzan == null) {
                        errorMessage = "データがTECHS部品表情報に存在しません"
                        writeLog("W", "スキャン解析エラー(現品票): 支給残数が不正です -> $chuzanStr")
                        clearInputFields()
                        return
                    }
                    shikyuYoteiSu = formattedChuzan
                    konkaiShikyuSu = formattedChuzan
                }
                scannedDenpyoShubetsu = "03"
                writeLog("I", "種別判定: 現品票検出 -> denpyoShubetsuCd='03', denpyoBango='$orderNo', targetSeihinBango='$targetSeihinBango', targetHinban='$targetHinban'")
            }
            tokens.size >= 2 && tokens[1] == "06" -> {
                val issueNo = tokens[0]
                denpyoBango = issueNo

                targetSeihinBango = if (tokens.size > 5) tokens[5] else ""
                targetHinban = if (tokens.size > 7) tokens[7] else ""

                seihinBango = targetSeihinBango
                hinban = targetHinban

                if (tokens.size >= 5) {
                    val tehaiSuStr = tokens[4]
                    val formattedTehaiSu = formatNumber(tehaiSuStr)
                    if (formattedTehaiSu == null) {
                        errorMessage = "データがTECHS部品表情報に存在しません"
                        writeLog("W", "スキャン解析エラー(払出伝票): 手配数が不正です -> $tehaiSuStr")
                        clearInputFields()
                        return
                    }
                    konkaiShikyuSu = formattedTehaiSu
                }
                scannedDenpyoShubetsu = "03"
                writeLog("I", "種別判定: 無償支給払出伝票検出 -> denpyoShubetsuCd='03', issueNo='$issueNo', targetSeihinBango='$targetSeihinBango', targetHinban='$targetHinban'")
            }
            tokens.size == 1 -> {
                errorMessage = "データがTECHS部品表情報に存在しません"
                writeLog("W", "スキャン解析エラー: 単体コードは許可されていません -> ${tokens[0]}")
                clearInputFields()
                return
            }
            else -> {
                errorMessage = "データがTECHS部品表情報に存在しません"
                writeLog("W", "スキャン解析エラー: 不明なトークン数です -> ${tokens.size}")
                clearInputFields()
                return
            }
        }

        executeBuhinHyoValidation(targetSeihinBango, targetBuhinKanriNo, targetHinban)
    }

    private fun clearInputFields() {
        writeLog("I", "clearInputFields実行")
        seihinBango = ""
        denpyoBango = ""
        buhinKanriNo = ""
        hinban = ""
        scannedDenpyoShubetsu = "03"
        shikyuYoteiSu = "0"
        konkaiShikyuSu = "0"
        tehaiSu = 0.0
        isDataLoadedForRegister = false
    }

    private fun executeBuhinHyoValidation(seihinBangoQuery: String, buhinKanriNoQuery: String, hinbanQuery: String) {
        writeLog("I", "executeBuhinHyoValidation開始: seihinBangoQuery='$seihinBangoQuery', buhinKanriNoQuery='$buhinKanriNoQuery', hinbanQuery='$hinbanQuery'")
        viewModelScope.launch {
            try {
                val buhinHyoResponse = apiService.getBuhinHyoInfo(
                    seihinBango = seihinBangoQuery.ifEmpty { null },
                    buhinKanriNo = buhinKanriNoQuery.ifEmpty { null },
                    hinban = hinbanQuery.ifEmpty { null }
                )

                if (!buhinHyoResponse.isSuccessful || buhinHyoResponse.body() == null) {
                    errorMessage = "データがTECHS部品表情報に存在しません"
                    isDataLoadedForRegister = false
                    writeLog("W", "部品表バリデーション失敗: APIレスポンスが不成功またはnullです (isSuccessful=${buhinHyoResponse.isSuccessful})")
                    return@launch
                }

                val buhinInfo = buhinHyoResponse.body()!!
                val fetchedSeihinBango = buhinInfo.seihinBango ?: seihinBangoQuery

                seihinBango = fetchedSeihinBango

                if (!buhinInfo.buhinKanriNo.isNullOrBlank()) {
                    buhinKanriNo = buhinInfo.buhinKanriNo
                }

                val fetchedTehaiSu = buhinInfo.tehaisu ?: 0.0
                tehaiSu = fetchedTehaiSu
                shikyuYoteiSu = if (fetchedTehaiSu % 1.0 == 0.0) {
                    fetchedTehaiSu.toInt().toString()
                } else {
                    String.format(java.util.Locale.US, "%.2f", fetchedTehaiSu).trimEnd('0').trimEnd('.')
                }

                writeLog("I", "部品表データ取得成功 -> seihinBango='$seihinBango', buhinKanriNo='$buhinKanriNo', tehaiSu=$tehaiSu")

                checkEmergency(fetchedSeihinBango)

                val boxCodeToCheck = shobunruiCode.ifEmpty { hokanBoxCode }
                if (boxCodeToCheck.isNotEmpty()) {
                    writeLog("I", "保管BOX使用状況チェック開始: boxCodeToCheck='$boxCodeToCheck'")
                    val boxCheckResponse = apiService.checkHokanBoxSiyo(boxCodeToCheck)
                    if (boxCheckResponse.isSuccessful) {
                        val usingSeihinBango = boxCheckResponse.body()?.seihinBango
                        writeLog("I", "保管BOX使用状況取得成功: usingSeihinBango='$usingSeihinBango'")
                        if (!usingSeihinBango.isNullOrBlank() && usingSeihinBango != fetchedSeihinBango) {
                            errorMessage = "既に他の製品No保管BOXを使用しています\n保管BOXを変更してください"
                            isDataLoadedForRegister = false
                            writeLog("W", "保管BOX重複エラー: 異なる製品Noで使用されています (using=$usingSeihinBango, current=$fetchedSeihinBango)")
                            return@launch
                        }
                    }
                }

                isDataLoadedForRegister = true
                infoMessage = "『登録』ボタンを押してください"
                writeLog("I", "バリデーション全完了 -> isRegisterEnabled(isDataLoadedForRegister) = true")

            } catch (e: Exception) {
                errorMessage = "ハードウェア異常 システム管理者に問合せ願います"
                isDataLoadedForRegister = false
                writeLog("E", "部品表バリデーション中に例外発生: ${e.message}", e)
            }
        }
    }

    private fun checkEmergency(targetSeihinBango: String) {
        writeLog("I", "checkEmergency開始: targetSeihinBango='$targetSeihinBango'")
        viewModelScope.launch {
            try {
                val response = apiService.checkEmergency(targetSeihinBango)
                if (response.isSuccessful) {
                    val kinkyuVal = response.body()?.kinkyu
                    isEmergency = !kinkyuVal.isNullOrBlank()
                    writeLog("I", "checkEmergency成功: kinkyuVal='$kinkyuVal', isEmergency=$isEmergency, isEmergencyButtonEnabled=$isEmergencyButtonEnabled")
                } else {
                    isEmergency = false
                    writeLog("W", "checkEmergency不成功: code=${response.code()}")
                }
            } catch (e: Exception) {
                isEmergency = false
                writeLog("E", "checkEmergency例外発生: ${e.message}", e)
            }
        }
    }

    fun executeRegister() {
        errorMessage = null
        infoMessage = null

        writeLog("I", "=== executeRegister開始 ===")
        writeLog("I", "送信パラメータ -> denpyoBango: '$denpyoBango', seihinBango: '$seihinBango', buhinKanriNo: '$buhinKanriNo', konkaiShikyuSu: '$konkaiShikyuSu', isEmergency: $isEmergency, gymTntMei: '$gymTntMei'")

        viewModelScope.launch {
            try {
                val konkaiSu = konkaiShikyuSu.toDoubleOrNull() ?: 0.0
                val denpyoShubetsu = scannedDenpyoShubetsu
                val kinkyuKbnValue = if (isEmergency) "2" else "1"

                val request = RegisterRequestModel(
                    denpyoShubetsuCd = denpyoShubetsu,
                    denpyoBango = denpyoBango,
                    seihinBango = seihinBango,
                    buhinKanriNo = buhinKanriNo,
                    hokanEriaCd = null,
                    hokanEriaMeisho = null,
                    hokanBoxCd = hokanBoxCode,
                    hokanBoxMeisho = hokanBoxMeisho,
                    hokanShizaiCd = hokanShizaiCode,
                    hokanShizaiMeisho = hokanShizaiMeisho,
                    konkaiShikyuSu = konkaiSu,
                    userCd = userCd,
                    kinkyuKbn = kinkyuKbnValue,
                    shiwakeTntMei = gymTntMei
                )

                writeLog("I", "登録API呼び出し中...")
                val response = apiService.registerData(request)
                writeLog("I", "登録APIレスポンス受信: isSuccessful=${response.isSuccessful}, code=${response.code()}")

                if (response.isSuccessful && response.body()?.success == true) {
                    infoMessage = "仕分け登録完了"
                    registeredItemCount++
                    isDataLoadedForRegister = false
                    writeLog("I", "【登録成功】 registeredItemCountがインクリメントされました -> 現在の件数: $registeredItemCount")
                } else {
                    val errorBodyString = response.errorBody()?.string()
                    writeLog("W", "登録APIエラーレスポンス: $errorBodyString")
                    val errorMsg = if (!errorBodyString.isNullOrEmpty()) {
                        try {
                            org.json.JSONObject(errorBodyString).optString("message", "登録処理に失敗しました")
                        } catch (e: Exception) {
                            "登録処理に失敗しました"
                        }
                    } else {
                        response.body()?.message ?: "登録処理に失敗しました"
                    }
                    errorMessage = errorMsg
                    writeLog("W", "登録失敗メッセージ設定: $errorMsg")
                }
            } catch (e: Exception) {
                errorMessage = "システム管理者に連絡してください"
                writeLog("E", "登録処理中に例外発生: ${e.message}", e)
            }
        }
    }

    fun executeDelete() {
        if (seihinBango.isBlank() && denpyoBango.isBlank()) {
            errorMessage = "削除対象のデータが読み込まれていません"
            writeLog("W", "executeDelete: 削除対象データが存在しません")
            return
        }

        errorMessage = null
        infoMessage = null

        val boxCd = shobunruiCode.ifEmpty { hokanBoxCode }
        val shizaiCd = chubunruiCode.ifEmpty { hokanShizaiCode }

        writeLog("I", "=== executeDelete開始 ===")
        writeLog("I", "削除パラメータ -> seihinBango: '$seihinBango', denpyoBango: '$denpyoBango', boxCd: '$boxCd', shizaiCd: '$shizaiCd'")

        viewModelScope.launch {
            try {
                val request = DeleteRequestModel(
                    denpyoBango = denpyoBango,
                    seihinBango = seihinBango,
                    hokanBoxCd = boxCd,
                    hokanShizaiCd = shizaiCd,
                    hasDetailData = true
                )

                writeLog("I", "削除API呼び出し中...")
                val response = apiService.deleteData(request)
                writeLog("I", "削除APIレスポンス受信: isSuccessful=${response.isSuccessful}, code=${response.code()}")

                if (response.isSuccessful && response.body()?.success == true) {
                    clearInput()
                    if (registeredItemCount > 0) {
                        registeredItemCount--
                    }
                    infoMessage = "削除完了"
                    writeLog("I", "【削除成功】残り件数: $registeredItemCount")
                } else {
                    val errorBodyString = response.errorBody()?.string()
                    val errorMsg = if (!errorBodyString.isNullOrEmpty()) {
                        try {
                            org.json.JSONObject(errorBodyString).optString("message", "削除処理に失敗しました")
                        } catch (e: Exception) {
                            "削除処理に失敗しました"
                        }
                    } else {
                        "削除処理に失敗しました"
                    }
                    errorMessage = errorMsg
                    writeLog("W", "削除失敗メッセージ設定: $errorMsg")
                }
            } catch (e: Exception) {
                errorMessage = "システム管理者に連絡してください"
                writeLog("E", "削除処理中に例外発生: ${e.message}", e)
            }
        }
    }

    fun setInitialSelection(chuCode: String, chuName: String, shoCode: String, shoName: String) {
        writeLog("I", "setInitialSelection開始 -> chuCode='$chuCode', chuName='$chuName', shoCode='$shoCode', shoName='$shoName'")
        if (chuCode.isNotEmpty()) {
            chubunruiCode = chuCode
            chubunruiMeisho = chuName
            hokanShizaiCode = chuCode
            hokanShizaiMeisho = chuName
            shobunruiCode = ""
            shobunruiMeisho = ""
            hokanBoxCode = ""
            hokanBoxMeisho = ""
            writeLog("I", "初期選択: 保管資材を設定しました (Code: $chuCode)")
        } else if (shoCode.isNotEmpty()) {
            shobunruiCode = shoCode
            shobunruiMeisho = shoName
            hokanBoxCode = shoCode
            hokanBoxMeisho = shoName
            chubunruiCode = ""
            chubunruiMeisho = ""
            hokanShizaiCode = ""
            hokanShizaiMeisho = ""
            writeLog("I", "初期選択: 保管BOXを設定しました (Code: $shoCode)")
        }
    }
}