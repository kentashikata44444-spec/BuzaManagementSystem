package com.example.buzaimanagementsystem.ui.theme.skl0101g01

import android.app.Application
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.buzaimanagementsystem.utils.FileLogger
import kotlinx.coroutines.launch

class SKL0101G01ViewModel(
    application: Application,
    private val apiService: SKL0101G01ApiService
) : AndroidViewModel(application) {

    companion object {
        private const val TAG = "SKL0101G01ViewModel"
    }

    var userCd by mutableStateOf("")
        private set
    var gymTntCd by mutableStateOf("")
        private set
    var gymTntMei by mutableStateOf("")
        private set
    var sourceDivision by mutableStateOf("1")
        private set

    var hokanBoxCode by mutableStateOf("")
        private set
    var hokanShizaiCode by mutableStateOf("")
        private set

    var chubunruiMeisho by mutableStateOf("")
        private set
    var shobunruiMeisho by mutableStateOf("")
        private set
    var seihinBango by mutableStateOf("")
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set
    var infoMessage by mutableStateOf<String?>(null)
        private set

    /**
     * 内部共通ログ出力用（Logcat ＆ テキストファイルへ同時出力）
     */
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

    /**
     * パラメータを初期化する
     */
    fun initParams(userCd: String, gymTntCd: String, gymTntMei: String, sourceDivision: String) {
        writeLog("D", "initParams: パラメータを初期化します (userCd=$userCd, gymTntCd=$gymTntCd, gymTntMei=$gymTntMei, sourceDivision=$sourceDivision)")
        this.userCd = userCd
        this.gymTntCd = gymTntCd
        this.gymTntMei = gymTntMei
        this.sourceDivision = sourceDivision
    }

    /**
     * 画面再表示時に入力値やメッセージをクリアする
     */
    fun clearInput() {
        writeLog("D", "clearInput: 入力内容とメッセージをクリアします")
        hokanBoxCode = ""
        hokanShizaiCode = ""
        chubunruiMeisho = ""
        shobunruiMeisho = ""
        seihinBango = ""
        errorMessage = null
        infoMessage = null
    }

    /**
     * スキャンされたQRコードを解析・処理する
     */
    fun processScannedCode(
        scannedText: String,
        onNavigateToShitei: (chuCode: String, chuName: String, shoCode: String, shoName: String) -> Unit
    ) {
        writeLog("D", "processScannedCode: QRコードの処理を開始します (scannedText=$scannedText)")
        errorMessage = null
        infoMessage = null

        val tokens = scannedText.split("|")
        if (tokens.isEmpty()) {
            writeLog("W", "processScannedCode: トークンが空です (scannedText=$scannedText)")
            errorMessage = "保管BOXまたは、保管資材以外が読み込まれました\n※『保管BOX』または『保管資材』のQRコードを読み込んでください"
            return
        }

        val prefix = tokens[0].trim()
        when (prefix) {
            "HSIZAI" -> {
                if (tokens.size < 2) {
                    writeLog("W", "processScannedCode: HSIZAIの形式が不正です (tokens.size=${tokens.size})")
                    errorMessage = "QRコードの形式が不正です"
                    return
                }
                // 不要なスペースを削除するよう .trim() を追加
                val chuCode = tokens[1].trim()
                writeLog("D", "processScannedCode: 保管資材コードを検知 (chuCode=$chuCode)")

                viewModelScope.launch {
                    try {
                        val masterResponse = apiService.getChuMaster(chuCode)
                        if (!masterResponse.isSuccessful || masterResponse.body() == null) {
                            writeLog("W", "processScannedCode: 保管資材がマスタに存在しません (chuCode=$chuCode)")
                            errorMessage = "読み込まれた保管資材がマスタに存在しません\n確認をしてください"
                            return@launch
                        }
                        val master = masterResponse.body()!!
                        chubunruiMeisho = master.chubunruiMeisho ?: ""
                        hokanShizaiCode = chuCode

                        if (chuCode.contains("JIKA")) {
                            writeLog("I", "processScannedCode: 自架置が読み込まれました (chuCode=$chuCode)")
                            infoMessage = "自架置が読み込まれました"
                            return@launch
                        }

                        val siyoResponse = apiService.getHokanShizaiSiyoJoho(chuCode)
                        if (!siyoResponse.isSuccessful || siyoResponse.body() == null) {
                            writeLog("D", "processScannedCode: 保管資材の使用情報なし。指定画面へ遷移します (chuCode=$chuCode)")
                            onNavigateToShitei(chuCode, chubunruiMeisho, "", "")
                        } else {
                            val data = siyoResponse.body()!!
                            writeLog("I", "processScannedCode: 保管資材の使用情報を取得 (seihinBango=${data.seihinBango})")
                            infoMessage = "保管資材には、取得した製品No.(${data.seihinBango})を保管しています"
                            seihinBango = data.seihinBango
                        }
                    } catch (e: Exception) {
                        writeLog("E", "processScannedCode: HSIZAI処理中の通信エラー", e)
                        errorMessage = "通信エラーが発生しました: ${e.localizedMessage}"
                    }
                }
            }
            "HBOX" -> {
                if (tokens.size < 2) {
                    writeLog("W", "processScannedCode: HBOXの形式が不正です (tokens.size=${tokens.size})")
                    errorMessage = "QRコードの形式が不正です"
                    return
                }
                // 不要なスペースを削除するよう .trim() を追加
                val shoCode = tokens[1].trim()
                writeLog("D", "processScannedCode: 保管BOXコードを検知 (shoCode=$shoCode)")

                viewModelScope.launch {
                    try {
                        val masterResponse = apiService.getShoMaster(shoCode)
                        if (!masterResponse.isSuccessful || masterResponse.body() == null) {
                            writeLog("W", "processScannedCode: 保管BOXがマスタに存在しません (shoCode=$shoCode)")
                            errorMessage = "読み込まれた保管BOXがマスタに存在しません\n確認をしてください"
                            return@launch
                        }
                        val master = masterResponse.body()!!
                        shobunruiMeisho = master.shobunruiMeisho ?: ""
                        hokanBoxCode = shoCode

                        val siyoResponse = apiService.getHokanBoxSiyoJoho(shoCode)
                        if (!siyoResponse.isSuccessful || siyoResponse.body() == null) {
                            writeLog("D", "processScannedCode: 保管BOXの使用情報なし。指定画面へ遷移します (shoCode=$shoCode)")
                            onNavigateToShitei("", "", shoCode, shobunruiMeisho)
                        } else {
                            val data = siyoResponse.body()!!
                            writeLog("I", "processScannedCode: 保管BOXの使用情報を取得 (seihinBango=${data.seihinBango})")
                            infoMessage = "保管BOXには、取得した製品No.(${data.seihinBango})を保管しています"
                            seihinBango = data.seihinBango
                        }
                    } catch (e: Exception) {
                        writeLog("E", "processScannedCode: HBOX処理中の通信エラー", e)
                        errorMessage = "通信エラーが発生しました: ${e.localizedMessage}"
                    }
                }
            }
            else -> {
                writeLog("W", "processScannedCode: 未知のプレフィックスです (prefix=$prefix)")
                errorMessage = "保管BOXまたは、保管資材以外が読み込まれました\n※『保管BOX』または『保管資材』のQRコードを読み込んでください"
            }
        }
    }
}