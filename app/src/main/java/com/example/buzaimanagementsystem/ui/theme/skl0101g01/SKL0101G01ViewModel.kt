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

    fun initParams(userCd: String, gymTntCd: String, gymTntMei: String, sourceDivision: String) {
        writeLog("D", "initParams: パラメータ初期化 (userCd=$userCd, gymTntCd=$gymTntCd, gymTntMei=$gymTntMei, sourceDivision=$sourceDivision)")
        this.userCd = userCd
        this.gymTntCd = gymTntCd
        this.gymTntMei = gymTntMei
        this.sourceDivision = sourceDivision
    }

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
     * スキャンされたQRコードを解析し、sourceDivision ("1", "2", "3") に応じて処理を分岐する
     */
    fun processScannedCode(
        scannedText: String,
        onNavigateToShitei: (userCd: String, gymTntCd: String, gymTntMei: String, chuCode: String, chuName: String, shoCode: String, shoName: String, sourceDiv: String) -> Unit
    ) {
        writeLog("D", "processScannedCode: 処理開始 (scannedText=$scannedText, sourceDivision=$sourceDivision)")
        errorMessage = null
        infoMessage = null

        val tokens = scannedText.split("|")
        if (tokens.isEmpty()) {
            errorMessage = "※『保管BOX』または『保管資材』のQRコードを\n読み込んでください"
            writeLog("W", "processScannedCode: トークンが空です - $errorMessage")
            return
        }

        val prefix = tokens[0].trim()
        when (prefix) {
            "HSIZAI" -> {
                if (tokens.size < 2) {
                    errorMessage = "QRコードの形式が不正です"
                    writeLog("W", "processScannedCode [HSIZAI]: QRコードの形式が不正です (tokens.size=${tokens.size})")
                    return
                }
                val chuCode = tokens[1].trim()

                viewModelScope.launch {
                    try {
                        val masterResponse = apiService.getChuMaster(chuCode)
                        if (!masterResponse.isSuccessful || masterResponse.body() == null) {
                            errorMessage = "読み込まれた保管資材がマスタに存在しません\n確認をしてください"
                            writeLog("W", "processScannedCode [HSIZAI]: 保管資材マスタが存在しません (chuCode=$chuCode)")
                            return@launch
                        }
                        val master = masterResponse.body()!!
                        chubunruiMeisho = master.chubunruiMeisho ?: ""
                        hokanShizaiCode = chuCode

                        if (chuCode.contains("JIKA")) {
                            infoMessage = "自架置が読み込まれました"
                            writeLog("I", "processScannedCode [HSIZAI]: 自架置が読み込まれました (chuCode=$chuCode)")
                            return@launch
                        }

                        // 遷移元区分に応じた画面名称の決定
                        val targetScreenName = when (sourceDivision) {
                            "1" -> "スマホ仕分け（神戸・筑波支給品現品確定）"
                            "2" -> "スマホ仕分け（鈴鹿支給品現品確定）"
                            "3" -> "スマホ仕分け（勝英購入品・倉庫品現品確定）"
                            else -> "スマホ仕分け（現品確定）"
                        }

                        infoMessage = "中分類の名称(${chubunruiMeisho})が取得できました"
                        writeLog("I", "processScannedCode [HSIZAI]: 保管資材の中分類マスタから名称を取得しました (chuCode=$chuCode, chubunruiMeisho=$chubunruiMeisho)。$targetScreenName へ遷移します。")

                        // 指定画面へ遷移
                        onNavigateToShitei(userCd, gymTntCd, gymTntMei, chuCode, chubunruiMeisho, "", "", sourceDivision)

                    } catch (e: Exception) {
                        writeLog("E", "HSIZAI処理中の通信エラー", e)
                        errorMessage = "通信エラーが発生しました: ${e.localizedMessage}"
                    }
                }
            }
            "HBOX" -> {
                if (tokens.size < 2) {
                    errorMessage = "QRコードの形式が不正です"
                    writeLog("W", "processScannedCode [HBOX]: QRコードの形式が不正です (tokens.size=${tokens.size})")
                    return
                }
                val shoCode = tokens[1].trim()

                viewModelScope.launch {
                    try {
                        val masterResponse = apiService.getShoMaster(shoCode)
                        if (!masterResponse.isSuccessful || masterResponse.body() == null) {
                            errorMessage = "読み込まれた保管BOXがマスタに存在しません\n確認をしてください"
                            writeLog("W", "processScannedCode [HBOX]: 保管BOXマスタが存在しません (shoCode=$shoCode)")
                            return@launch
                        }
                        val master = masterResponse.body()!!
                        shobunruiMeisho = master.shobunruiMeisho ?: ""
                        hokanBoxCode = shoCode

                        // 遷移元区分に応じた画面名称の決定
                        val targetScreenName = when (sourceDivision) {
                            "1" -> "スマホ仕分け（神戸・筑波支給品現品確定）"
                            "2" -> "スマホ仕分け（鈴鹿支給品現品確定）"
                            "3" -> "スマホ仕分け（勝英購入品・倉庫品現品確定）"
                            else -> "スマホ仕分け（現品確定）"
                        }

                        infoMessage = "小分類の名称(${shobunruiMeisho})が取得できました"
                        writeLog("I", "processScannedCode [HBOX]: 保管BOXの小分類マスタから名称を取得しました (shoCode=$shoCode, shobunruiMeisho=$shobunruiMeisho)。$targetScreenName へ遷移します。")

                        // 指定画面へ遷移
                        onNavigateToShitei(userCd, gymTntCd, gymTntMei, "", "", shoCode, shobunruiMeisho, sourceDivision)

                    } catch (e: Exception) {
                        writeLog("E", "HBOX処理中の通信エラー", e)
                        errorMessage = "通信エラーが発生しました: ${e.localizedMessage}"
                    }
                }
            }
            else -> {
                errorMessage = "※『保管BOX』または『保管資材』のQRコードを\n読み込んでください"
                writeLog("W", "processScannedCode: 不明なプレフィックスです (prefix=$prefix)")
            }
        }
    }
}