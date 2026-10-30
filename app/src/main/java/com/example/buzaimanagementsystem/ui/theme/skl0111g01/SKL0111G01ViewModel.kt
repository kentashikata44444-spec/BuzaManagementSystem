package com.example.buzaimanagementsystem.ui.theme.skl0111g01

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

class SKL0111G01ViewModel(
    application: Application,
    private val apiService: SKL0111G01ApiService? = null
) : AndroidViewModel(application) {

    companion object {
        private const val TAG = "SKL0111G01ViewModel"
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

    // 画面表示用の状態（一時保管: 保管エリア確定画面用）
    var hokanAreaName by mutableStateOf("")
        private set
    var hokanShizaiName by mutableStateOf("")
        private set
    var targetHokanAreaCd by mutableStateOf("")
        private set
    var targetHokanShizaiCd by mutableStateOf("")
        private set

    // 大分類マスタから取得する倉庫情報（登録API送信に必要）
    var targetSokoCd by mutableStateOf("")
        private set
    var targetSokoMeisho by mutableStateOf("")
        private set

    // 明細候補リストを保持する状態
    var meisaiList by mutableStateOf<List<SKL0111MeisaiItemModel>>(emptyList())
        private set

    // ★ 仕分け・一時保管メニューボタンの有効状態（明細一覧が0件のときのみ true）
    val isMenuButtonEnabled: Boolean
        get() = meisaiList.isEmpty()

    // メッセージエリア（保管エリアメッセージ / 保管資材メッセージ）
    var areaMessage by mutableStateOf("")
        private set
    var shizaiMessage by mutableStateOf("")
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

        // 初期表示時：保管エリアガイドメッセージを設定し、対象データを取得
        areaMessage = "※『保管エリア』のQRコードを読み込んでください"
        shizaiMessage = ""
        meisaiList = emptyList()
        fetchMeisaiData()
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

    // 部材所在明細情報の取得処理
    private fun fetchMeisaiData() {
        writeLog("I", "fetchMeisaiData 開始: seihinBango=$seihinBango")
        viewModelScope.launch {
            try {
                if (apiService == null) {
                    writeLog("W", "apiServiceがnullのため、モックデータで動作します")
                    return@launch
                }

                val response = apiService.getMeisaiInfo(seihinBango)
                if (response.isSuccessful && response.body() != null) {
                    val data = response.body()!!
                    if (data.success) {
                        val filteredList = (data.data ?: emptyList()).filter { item ->
                            !item.code.isNullOrBlank() &&
                                    !item.name.isNullOrBlank() &&
                                    item.code.trim() != ":" &&
                                    item.name.trim() != ":"
                        }
                        meisaiList = filteredList

                        // ★ 取得成功であってもリストが0件の場合は「対象データはありません」を表示する
                        if (filteredList.isEmpty()) {
                            areaMessage = "対象データはありません"
                            writeLog("W", "明細データは0件です")
                        } else {
                            areaMessage = "" // データがある場合はガイドをクリア
                            writeLog("I", "明細データ取得成功 -> 候補件数=${meisaiList.size}")
                        }
                    } else {
                        areaMessage = data.message ?: "対象データはありません"
                        writeLog("W", "明細データ取得失敗: ${data.message}")
                    }
                } else {
                    areaMessage = "対象データはありません"
                    writeLog("W", "明細データ取得APIレスポンスエラー: code=${response.code()}")
                }
            } catch (e: Exception) {
                writeLog("E", "明細データ取得中に例外発生: ${e.message}", e)
                areaMessage = "システム管理者に連絡してください"
            }
        }
    }

    // 保管エリア変更ボタン押下時の初期化処理（※明細一覧はクリアしない）
    fun clearHokanArea() {
        writeLog("I", "保管エリア変更ボタン押下: 保管エリア情報を初期化します")
        targetHokanAreaCd = ""
        hokanAreaName = ""
        targetSokoCd = ""
        targetSokoMeisho = ""
        areaMessage = "※『保管エリア』のQRコードを読み込んでください"
        shizaiMessage = ""
        errorMessage = null
        infoMessage = null
    }

    // 処理済みアイテムをリストから除外するヘルパー
    private fun removeProcessedItem(code: String) {
        meisaiList = meisaiList.filter { it.code != code }
        writeLog("I", "処理済みアイテムをリストから除外しました: code=$code, 残り件数=${meisaiList.size}")

        // ★ 残り件数が0件になった場合
        if (meisaiList.isEmpty()) {
            areaMessage = "『メインメニュー』のボタンを押下してください"
            shizaiMessage = "" // 保管資材メッセージをクリア

            // 保管エリアの値もクリアする
            targetHokanAreaCd = ""
            hokanAreaName = ""
            targetSokoCd = ""
            targetSokoMeisho = ""

            writeLog("I", "すべての明細が処理されました：保管エリア情報をクリアし、メインメニュー誘導を表示")
        } else {
            // まだ明細が残っている場合は、次の資材読み込みを促す
            shizaiMessage = "※『保管資材』のQRコードを読み込んでください"
        }
    }

    // データベースの登録更新処理
    private fun executeDatabaseRegistration(areaCd: String, shizaiCd: String) {
        viewModelScope.launch {
            try {
                if (apiService == null) {
                    writeLog("W", "apiServiceがnullのためデータベース登録をスキップします")
                    return@launch
                }

                val request = SKL0111RegisterRequestModel(
                    seihinBango = seihinBango,
                    hokanShizaiCd = shizaiCd,
                    sokoCd = targetSokoCd,
                    sokoMeisho = targetSokoMeisho,
                    hokanEriaCd = areaCd,
                    hokanEriaMeisho = hokanAreaName,
                    userCd = userCd
                )

                writeLog("I", "データベース登録API呼び出し開始 -> seihinBango=$seihinBango, areaCd=$areaCd, shizaiCd=$shizaiCd, sokoCd=$targetSokoCd")
                val response = apiService.registerHokanArea(request)

                if (response.isSuccessful && response.body() != null) {
                    val res = response.body()!!
                    if (res.success) {
                        infoMessage = res.message ?: "保管エリアを確定しました"
                        writeLog("I", "データベース登録成功: ${res.message}")
                        removeProcessedItem(shizaiCd)
                        targetHokanShizaiCd = ""
                        hokanShizaiName = ""

                        // ★ 修正: まだ明細が残っている場合のみ、次の資材読み込みを促す
                        if (meisaiList.isNotEmpty()) {
                            shizaiMessage = "※『保管資材』のQRコードを読み込んでください"
                        } else {
                            shizaiMessage = "" // 0件の場合は完全にクリアする
                        }

                    } else {
                        shizaiMessage = res.message ?: "登録処理に失敗しました"
                        writeLog("W", "データベース登録失敗: ${res.message}")
                    }
                } else {
                    shizaiMessage = "データベース登録のレスポンスエラー"
                    writeLog("W", "登録APIレスポンスエラー: code=${response.code()}")
                }
            } catch (e: Exception) {
                writeLog("E", "データベース登録中に例外発生: ${e.message}", e)
                shizaiMessage = "データ登録に失敗しました\nシステム管理者に連絡してください"
            }
        }
    }

    fun processScannedCode(scannedText: String) {
        writeLog("I", "processScannedCode 開始: scannedText=$scannedText")
        errorMessage = null
        infoMessage = null
        areaMessage = ""
        shizaiMessage = ""

        val tokens = scannedText.split("|").map { it.trim() }
        if (tokens.isEmpty()) {
            if (targetHokanAreaCd.isEmpty()) {
                areaMessage = "保管エリアまたは、保管資材以外が読み込まれました"
            } else {
                shizaiMessage = "保管エリアまたは、保管資材以外が読み込まれました"
            }
            writeLog("W", "スキャンエラー: トークンが空です")
            return
        }

        val qrType = tokens[0]
        writeLog("I", "読み込まれた種類: $qrType")

        if (tokens.size < 2) {
            if (targetHokanAreaCd.isEmpty()) {
                areaMessage = "コードが設定されていません"
            } else {
                shizaiMessage = "コードが設定されていません"
            }
            writeLog("W", "スキャンエラー: コードが含まれていません")
            return
        }

        val scannedCode = tokens[1]

        if (qrType == "HAREA" || targetHokanAreaCd.isEmpty()) {
            validateAndRegisterArea(scannedCode, tokens)
        } else {
            validateAndRegisterShizai(scannedCode, tokens)
        }
    }

    private fun validateAndRegisterArea(areaCode: String, tokens: List<String>) {
        writeLog("I", "validateAndRegisterArea 開始: areaCode=$areaCode")

        val qrType = if (tokens.isNotEmpty()) tokens[0] else ""
        if (qrType != "HAREA") {
            areaMessage = "保管エリア以外が読み込まれました\n※『保管エリア』のQRコードを読み込んでください"
            writeLog("W", "保管エリアチェックエラー: HAREAではありません ($qrType)")
            return
        }

        if (targetHokanAreaCd.isNotEmpty()) {
            areaMessage = "保管エリアの変更時は、保管エリア変更ボタンを押してください"
            writeLog("W", "保管エリア変更エラー: すでに設定されています")
            return
        }

        viewModelScope.launch {
            try {
                if (apiService == null) {
                    targetHokanAreaCd = areaCode
                    hokanAreaName = "保管エリア_$areaCode"
                    targetSokoCd = "00"
                    targetSokoMeisho = "勝英産業"
                    areaMessage = ""
                    shizaiMessage = "※『保管資材』を読み込んでください"
                    return@launch
                }

                val response = apiService.getDaiMaster(areaCode)
                if (response.isSuccessful && response.body() != null && response.body()!!.success) {
                    val res = response.body()!!
                    targetHokanAreaCd = res.hokanEriaCd ?: areaCode
                    hokanAreaName = res.hokanEriaMeisho ?: areaCode
                    targetSokoCd = res.sokoCd ?: ""
                    targetSokoMeisho = res.sokoMeisho ?: ""

                    areaMessage = ""
                    shizaiMessage = "※『保管資材』を読み込んでください"
                    writeLog("I", "保管エリア設定成功: AreaName=$hokanAreaName, SokoCd=$targetSokoCd")

                    if (targetHokanShizaiCd.isNotEmpty()) {
                        executeDatabaseRegistration(targetHokanAreaCd, targetHokanShizaiCd)
                    }
                } else {
                    areaMessage = "読み込まれた保管エリアが存在しません"
                    writeLog("W", "保管エリアマスタ検索失敗: code=$areaCode")
                }
            } catch (e: Exception) {
                writeLog("E", "保管エリア検証中に例外発生: ${e.message}", e)
                areaMessage = "システム管理者に連絡してください"
            }
        }
    }

    private fun validateAndRegisterShizai(shizaiCode: String, tokens: List<String>) {
        writeLog("I", "validateAndRegisterShizai 開始: shizaiCode=$shizaiCode")

        if (targetHokanAreaCd.isEmpty()) {
            areaMessage = "保管エリアが設定されていません\n※『保管エリア』のQRコードを読み込んでください"
            writeLog("W", "保管資材検証失敗: 保管エリアが未設定です")
            return
        }

        val matchedItem = meisaiList.find { it.code == shizaiCode || it.name.contains(shizaiCode) }
        if (matchedItem == null) {
            shizaiMessage = "読み込まれた保管資材は、対象外です"
            writeLog("W", "保管資材検証失敗: 対象外です (code=$shizaiCode)")
            return
        }

        hokanShizaiName = matchedItem.name
        targetHokanShizaiCd = matchedItem.code
        shizaiMessage = ""
        writeLog("I", "保管資材設定成功: 名称=${matchedItem.name}")

        executeDatabaseRegistration(targetHokanAreaCd, targetHokanShizaiCd)
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