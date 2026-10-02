package com.example.buzaimanagementsystem.ui.theme.skl0100g01

import android.app.Application
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.buzaimanagementsystem.utils.FileLogger
import kotlinx.coroutines.launch

class SKL0100G01ViewModel(
    application: Application
) : AndroidViewModel(application) {

    companion object {
        private const val TAG = "SKL0100G01ViewModel"
    }

    var userCd by mutableStateOf("")
        private set
    var gymTntCd by mutableStateOf("")
        private set
    var gymTntMei by mutableStateOf("")
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
     * メニュー画面から受け取った基本情報を初期化する
     */
    fun initParams(userCd: String, gymTntCd: String, gymTntMei: String) {
        writeLog("D", "initParams: パラメータを初期化します (userCd=$userCd, gymTntCd=$gymTntCd, gymTntMei=$gymTntMei)")
        this.userCd = userCd
        this.gymTntCd = gymTntCd
        this.gymTntMei = gymTntMei
    }

    /**
     * ボタン押下時の処理
     */
    fun onButtonClicked(onNavigate: () -> Unit) {
        writeLog("D", "onButtonClicked: ボタンが押下されました")
        viewModelScope.launch {
            onNavigate()
        }
    }
}