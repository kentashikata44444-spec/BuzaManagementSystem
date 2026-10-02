package com.example.buzaimanagementsystem.ui.theme.skl0001g01

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.example.buzaimanagementsystem.ui.theme.BuzaiManagementSystemTheme
import com.example.buzaimanagementsystem.BuildConfig
import com.example.buzaimanagementsystem.utils.FileLogger
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class SKL0001G01Activity : ComponentActivity() {

    companion object {
        private const val TAG = "SKL0001G01Activity"
    }

    // ▼ AndroidViewModelに対応させ、ApplicationとapiServiceを渡すFactory構成に修正
    private val viewModel: SKL0001G01ViewModel by viewModels {
        object : androidx.lifecycle.ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                Log.d(TAG, "ViewModel Factory: SKL0001G01ViewModel を生成します")
                val retrofit = Retrofit.Builder()
                    .baseUrl(BuildConfig.SERVER_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
                val apiService = retrofit.create(SKL0001G01ApiService::class.java)

                @Suppress("UNCHECKED_CAST")
                return SKL0001G01ViewModel(application, apiService) as T
            }
        }
    }

    private fun writeLog(level: String, message: String, throwable: Throwable? = null) {
        when (level) {
            "D" -> {
                Log.d(TAG, message, throwable)
                FileLogger.writeLog(applicationContext, "DEBUG", TAG, message + (throwable?.let { " : ${it.message}" } ?: ""))
            }
            "W" -> {
                Log.w(TAG, message, throwable)
                FileLogger.writeLog(applicationContext, "WARN", TAG, message + (throwable?.let { " : ${it.message}" } ?: ""))
            }
            "E" -> {
                Log.e(TAG, message, throwable)
                FileLogger.writeLog(applicationContext, "ERROR", TAG, message + (throwable?.let { " : ${it.message}" } ?: ""))
            }
            "I" -> {
                Log.i(TAG, message, throwable)
                FileLogger.writeLog(applicationContext, "INFO", TAG, message + (throwable?.let { " : ${it.message}" } ?: ""))
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        writeLog("D", "onCreate: SKL0001G01Activity が起動しました")

        // 画面表示時にシステム稼働状況チェックを実行
        viewModel.checkSystemAvailability()

        setContent {
            BuzaiManagementSystemTheme {
                SKL0001G01Screen(
                    viewModel = viewModel,
                    onCloseApp = {
                        writeLog("I", "onCloseApp: アプリを終了します")
                        finish()
                    },
                    onLoginSuccess = { userCd, userName, gymTntCd, gymTntMei ->
                        writeLog("I", "onLoginSuccess: ログイン成功コールバックを受信 (userCd=$userCd, name=$userName)")
                        // ログイン成功時の画面遷移などの処理をここに記述
                    }
                )
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        writeLog("D", "onDestroy: SKL0001G01Activity が破棄されました")
    }
}