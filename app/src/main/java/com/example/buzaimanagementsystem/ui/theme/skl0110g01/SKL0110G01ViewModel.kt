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

    // ★ 保管BOX候補リストを保持する状態（資材読み込み後にリストを展開する仕様）
    var hokanBoxList by mutableStateOf<List<HokanBoxItemModel>>(emptyList())
        private set

    // メッセージエリア
    var shizaiMessage by mutableStateOf("")
        private set
    var boxMessage by mutableStateOf("")
        private set

    // ★ 追加: 対象データが0件のときに次の画面へ自動遷移させるためのコールバック
    var onNavigateToNextScreen: (() -> Unit)? = null

    // ★ 追加: データ取得中かどうかを示すフラグ（初期値 true）
    var isLoading by mutableStateOf(true)
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

        // ★ 画面初期化時にリストを確実にクリアしてリフレッシュ
        hokanBoxList = emptyList()

        // ガイドメッセージの設定
        shizaiMessage = "※『保管資材』を読み込んでください"

        // 画面初期化（遷移）時に保管BOX候補データを取得する
        fetchHokanDataForShizai()
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

    private fun fetchHokanDataForShizai() {
        writeLog("I", "fetchHokanDataForShizai 開始: seihinBango=$seihinBango")
        isLoading = true // 取得開始時にロード中にする
        viewModelScope.launch {
            try {
                if (apiService == null) {
                    writeLog("W", "apiServiceがnullのため、モックデータで動作します")
                    registeredCount = 0
                    isLoading = false
                    return@launch
                }

                val response = apiService.getHokanInfo(seihinBango)
                if (response.isSuccessful && response.body() != null) {
                    val data = response.body()!!

                    if (data.success) {
                        registeredCount = data.count
                        hokanBoxList = data.data ?: emptyList()

                        // 0件の場合は即座に遷移（isLoadingはfalseにせずそのまま遷移へ）
                        if (data.count <= 0 || hokanBoxList.isEmpty()) {
                            writeLog("I", "【自動遷移判定】保管資材確定対象データが0件のため、自動で一時保管(保管エリア確定)画面へ遷移します")
                            onNavigateToNextScreen?.invoke()
                            return@launch
                        }

                        if (targetHokanShizaiCd.isNotEmpty()) {
                            shizaiMessage = ""
                        } else {
                            shizaiMessage = "※『保管資材』を読み込んでください"
                        }

                        errorMessage = null
                        isLoading = false // 正常にデータがある場合のみ画面を表示する
                        writeLog("I", "保管情報取得成功 -> 候補件数=${hokanBoxList.size}")
                    } else {
                        writeLog("I", "【自動遷移判定】保管資材確定対象データが存在しないため、自動で一時保管画面へ遷移します")
                        onNavigateToNextScreen?.invoke()
                        return@launch
                    }
                } else {
                    isLoading = false
                    shizaiMessage = "部材所在明細情報の取得に失敗しました\nシステム管理者に連絡してください"
                    hokanBoxList = emptyList()
                }
            } catch (e: Exception) {
                isLoading = false
                writeLog("E", "保管情報取得中に例外発生: ${e.message}", e)
                shizaiMessage = "部材所在明細情報の取得に失敗しました\nシステム管理者に連絡してください"
                hokanBoxList = emptyList()
            }
        }
    }

    // ★ 保管資材変更ボタン押下時の初期化処理（資材・BOX・メッセージ・リストをすべて初期化）
    fun clearHokanShizai() {
        writeLog("I", "保管資材変更ボタン押下: 保管資材情報を初期化します")
        targetHokanShizaiCd = ""
        hokanShizaiName = ""
        targetHokanBoxCd = ""
        hokanBoxName = ""
        shizaiMessage = "※『保管資材』を読み込んでください"
        boxMessage = ""
        errorMessage = null
        infoMessage = null
    }

    // ★ 処理済みアイテムをリストから除外するヘルパー
    private fun removeProcessedItem(boxCd: String) {
        hokanBoxList = hokanBoxList.filter { it.hokanBoxCd != boxCd }
        writeLog("I", "処理済みアイテムをリストから除外しました: boxCd=$boxCd, 残り件数=${hokanBoxList.size}")
    }

    // ★ データベースのテーブルを更新（登録）するAPI呼び出し処理
    private fun executeDatabaseRegistration(boxCd: String, shizaiCd: String) {
        viewModelScope.launch {
            try {
                if (apiService == null) {
                    writeLog("W", "apiServiceがnullのためデータベース登録をスキップします")
                    return@launch
                }

                val request = SKL0111RegisterRequestModel(
                    seihinBango = seihinBango,
                    hokanBoxCd = boxCd,
                    hokanShizaiCd = shizaiCd,
                    userCd = userCd
                )

                writeLog("I", "データベース登録API呼び出し開始 -> seihinBango=$seihinBango, boxCd=$boxCd, shizaiCd=$shizaiCd")
                val response = apiService.registerHokanArea(request)

                if (response.isSuccessful && response.body() != null) {
                    val res = response.body()!!
                    if (res.success) {
                        // 更新完了メッセージを表示
                        infoMessage = res.message ?: "保管エリアを確定しました"
                        writeLog("I", "データベース登録成功: ${res.message}")

                        // 登録成功時にリストから除外して画面を更新
                        removeProcessedItem(boxCd)
                    } else {
                        errorMessage = res.message ?: "部材所在明細情報の更新に失敗しました\nシステム管理者に連絡してください"
                        writeLog("W", "データベース登録失敗: ${res.message}")
                    }
                } else {
                    errorMessage = "部材所在明細情報の更新に失敗しました\nシステム管理者に連絡してください"
                    writeLog("W", "登録APIレスポンスエラー: code=${response.code()}")
                }
            } catch (e: Exception) {
                writeLog("E", "データベース登録中に例外発生: ${e.message}", e)
                errorMessage = "部材所在明細情報の更新に失敗しました\nシステム管理者に連絡してください"
            }
        }
    }

    fun processScannedCode(scannedText: String) {
        writeLog("I", "processScannedCode 開始: scannedText=$scannedText")
        errorMessage = null
        infoMessage = null
        shizaiMessage = ""
        boxMessage = ""

        val tokens = scannedText.split("|").map { it.trim() }
        if (tokens.isEmpty()) {
            if (targetHokanShizaiCd.isEmpty()) {
                shizaiMessage = "保管BOXまたは、保管資材以外が読み込まれました"
            } else {
                errorMessage = "保管BOXまたは、保管資材以外が読み込まれました"
            }
            writeLog("W", "スキャンエラー: トークンが空です")
            return
        }

        val hokanShurui = tokens[0]
        writeLog("I", "読み込まれた保管種類: $hokanShurui")

        if (hokanShurui != "HSIZAI" && hokanShurui != "HBOX") {
            if (targetHokanShizaiCd.isEmpty()) {
                shizaiMessage = "保管BOXまたは、保管資材以外が読み込まれました"
            } else {
                errorMessage = "保管BOXまたは、保管資材以外が読み込まれました"
            }
            writeLog("W", "保管種類エラー: 想定外のQRが読み込まれました ($hokanShurui)")
            return
        }

        if (tokens.size < 2) {
            if (targetHokanShizaiCd.isEmpty()) {
                shizaiMessage = "コードが設定されていません"
            } else {
                errorMessage = "コードが設定されていません"
            }
            writeLog("W", "スキャンエラー: コードが含まれていません")
            return
        }

        val scannedCode = tokens[1]
        writeLog("I", "取得したコード: $scannedCode")

        if (hokanShurui == "HSIZAI") {
            validateAndRegisterShizai(scannedCode, tokens)
        } else {
            validateAndRegisterBox(scannedCode, tokens)
        }
    }

    private fun validateAndRegisterShizai(shizaiCd: String, tokens: List<String>) {
        writeLog("I", "validateAndRegisterShizai 開始: shizaiCd=$shizaiCd")

        // ★ 保管資材がすでに設定されている場合は、連続読み取りを防ぐために処理をスキップ
        if (targetHokanShizaiCd.isNotEmpty()) {
            writeLog("I", "すでに保管資材が設定されているため、再読み取りをスキップします")
            return
        }

        viewModelScope.launch {
            try {
                if (apiService == null) {
                    errorMessage = "APIサービスが初期化されていません"
                    return@launch
                }

                val chubunruiCd = if (tokens.size > 1) tokens[1] else shizaiCd
                writeLog("I", "中分類マスタ検索実行: chubunruiCd=$chubunruiCd")

                val chuResponse = apiService.getChuMaster(chubunruiCd)
                if (!chuResponse.isSuccessful || chuResponse.body() == null || !chuResponse.body()!!.success) {
                    shizaiMessage = "読み込まれた保管資材がマスタに存在しません\n確認してください"
                    writeLog("W", "保管資材マスタ検索失敗: code=$chubunruiCd")
                    return@launch
                }

                val chubunruiMeisho = chuResponse.body()!!.chubunruiMeisho ?: shizaiCd
                hokanShizaiName = chubunruiMeisho
                targetHokanShizaiCd = chubunruiCd

                // ★ 修正: 保管資材読み込み成功時は、保管資材メッセージをクリアし、保管BOXメッセージにガイドを表示
                shizaiMessage = ""
                boxMessage = "※『保管BOX』を読み込んでください"

                infoMessage = ""

                // ★ 保管資材が読み込まれたタイミングでリストを取得・表示する
                fetchHokanDataForShizai()

                // 保管BOXが既に選択されている場合は、資材設定時にデータベース登録（確定）を実行
                if (targetHokanBoxCd.isNotEmpty()) {
                    executeDatabaseRegistration(targetHokanBoxCd, targetHokanShizaiCd)
                }

                writeLog("I", "保管資材設定成功: 名称=$chubunruiMeisho")

            } catch (e: Exception) {
                writeLog("E", "保管資材検証中に例外発生: ${e.message}", e)
                shizaiMessage = "保管資材の取得に失敗しました\nシステム管理者に連絡してください"
            }
        }
    }

    private fun validateAndRegisterBox(boxCd: String, tokens: List<String>) {
        writeLog("I", "validateAndRegisterBox 開始: boxCd=$boxCd")

        // 保管資材が未設定の場合
        if (targetHokanShizaiCd.isEmpty()) {
            shizaiMessage = "保管資材が設定されていません\n保管資材を設定してください"
            writeLog("W", "保管BOX検証失敗: 保管資材が設定されていません")
            return
        }

        // ★ 仕様書のフォーマット (HBOX | 小分類コード | 小分類名称) に合わせてコードを正確に取得
        // tokens[1] がコード（例: 001）、tokens[2] が名称（例: BOX001）
        val boxCode = if (tokens.size > 1) tokens[1] else boxCd
        writeLog("I", "検証対象BOXコード: $boxCode")

        viewModelScope.launch {
            try {
                if (apiService == null) {
                    errorMessage = "APIサービスが初期化されていません"
                    return@launch
                }

                // 1. ロケーション小分類マスタの存在チェック（正しいコード `boxCode` で検索）
                writeLog("I", "保管BOXマスタ検索実行: boxCd=$boxCode")
                val boxResponse = apiService.getBoxInfo(boxCode)

                if (!boxResponse.isSuccessful || boxResponse.body() == null || !boxResponse.body()!!.success) {
                    boxMessage = "読み込まれた保管BOXがマスタに存在しません\n確認してください"
                    writeLog("W", "保管BOXマスタ検索失敗: code=$boxCode")
                    return@launch
                }

                val boxMeisho = boxResponse.body()!!.hokanBoxMeisho ?: boxCode

                // 2. 変数テーブル（hokanBoxList）の中に該当のBOXが存在するかチェック
                // コード（hokanBoxCd）または名称（hokanBoxMeisho）のどちらに一致してもOKとする
                val matchedBox = hokanBoxList.find { it.hokanBoxCd == boxCode || it.hokanBoxMeisho == boxCode || it.hokanBoxCd == boxCd || it.hokanBoxMeisho == boxMeisho }

                if (matchedBox == null) {
                    boxMessage = "読み込まれた保管BOXが製品No.内に存在しません\n保管BOXを確認してください"
                    writeLog("W", "保管BOX検証失敗: 製品No.内に存在しません (code=$boxCode)")
                    return@launch
                }

                // 存在する場合は設定
                targetHokanBoxCd = matchedBox.hokanBoxCd
                hokanBoxName = matchedBox.hokanBoxMeisho
                boxMessage = "" // エラークリア
                writeLog("I", "保管BOX設定成功: cd=${matchedBox.hokanBoxCd}, 名称=${matchedBox.hokanBoxMeisho}")

                // データベース登録を実行
                executeDatabaseRegistration(targetHokanBoxCd, targetHokanShizaiCd)

            } catch (e: Exception) {
                writeLog("E", "保管BOX検証中に例外発生: ${e.message}", e)
                boxMessage = "保管BOXの取得に失敗しました\nシステム管理者に連絡してください"
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