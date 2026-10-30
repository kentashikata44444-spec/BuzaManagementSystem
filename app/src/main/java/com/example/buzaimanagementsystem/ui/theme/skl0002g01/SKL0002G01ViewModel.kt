package com.example.buzaimanagementsystem.ui.theme.skl0002g01

import android.app.Application
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.buzaimanagementsystem.utils.FileLogger
import kotlinx.coroutines.launch

class SKL0002G01ViewModel(
    application: Application
) : AndroidViewModel(application) {

    companion object {
        private const val TAG = "SKL0002G01ViewModel"
    }

    var userCd by mutableStateOf("")
        private set

    var userName by mutableStateOf("")
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
     * ユーザーおよび業務担当情報を初期化する
     */
    fun initUserData(userCd: String, userName: String, gymTntCd: String, gymTntMei: String) {
        writeLog("D", "initUserData: ユーザー情報を初期化します (userCd=$userCd, userName=$userName, gymTntCd=$gymTntCd, gymTntMei=$gymTntMei)")
        this.userCd = userCd
        this.userName = userName
        this.gymTntCd = gymTntCd
        this.gymTntMei = gymTntMei
    }

    /**
     * 仕訳ボタン押下時の処理
     */
    fun onShiwakeClicked(onNavigate: () -> Unit) {
        writeLog("D", "onShiwakeClicked: 仕分け画面へ遷移します")
        viewModelScope.launch { onNavigate() }
    }

    /**
     * 集約ボタン押下時の処理
     */
    fun onShuyakuClicked(onNavigate: () -> Unit) {
        writeLog("D", "onShuyakuClicked: 集約画面へ遷移します")
        viewModelScope.launch { onNavigate() }
    }

    /**
     * 引渡ボタン押下時の処理
     */
    fun onHikiwatashiClicked(onNavigate: () -> Unit) {
        writeLog("D", "onHikiwatashiClicked: 引渡画面へ遷移します")
        viewModelScope.launch { onNavigate() }
    }

    /**
     * 返却ボタン押下時の処理
     */
    fun onHenkyakuClicked(onNavigate: () -> Unit) {
        writeLog("D", "onHenkyakuClicked: 返却画面へ遷移します")
        viewModelScope.launch { onNavigate() }
    }

    /**
     * 個別ボタン押下時の処理
     */
    fun onKobetsuClicked(onNavigate: () -> Unit) {
        writeLog("D", "onKobetsuClicked: 個別画面へ遷移します")
        viewModelScope.launch { onNavigate() }
    }

    /**
     * 終了ボタン押下時の処理
     */
    fun onExitClicked(onExit: () -> Unit) {
        writeLog("I", "onExitClicked: 終了処理を実行します")
        viewModelScope.launch { onExit() }
    }
}