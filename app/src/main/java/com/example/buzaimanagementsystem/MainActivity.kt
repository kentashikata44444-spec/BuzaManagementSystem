package com.example.buzaimanagementsystem

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.example.buzaimanagementsystem.ui.theme.BuzaiManagementSystemTheme
import androidx.lifecycle.viewmodel.compose.viewModel

// SKL0001G01（ログイン画面）関連
import com.example.buzaimanagementsystem.ui.theme.skl0001g01.SKL0001G01Screen
import com.example.buzaimanagementsystem.ui.theme.skl0001g01.SKL0001G01ApiService
import com.example.buzaimanagementsystem.ui.theme.skl0001g01.SKL0001G01ViewModel

// SKL0002G01（メニュー画面）関連
import com.example.buzaimanagementsystem.ui.theme.skl0002g01.SKL0002G01Screen
import com.example.buzaimanagementsystem.ui.theme.skl0002g01.SKL0002G01ViewModel as SKL0002ViewModel

// SKL0100G01（仕分けメニュー画面）関連
import com.example.buzaimanagementsystem.ui.theme.skl0100g01.SKL0100G01Screen
import com.example.buzaimanagementsystem.ui.theme.skl0100g01.SKL0100G01ViewModel as SKL0100ViewModel

// SKL0101G01（仕分け：保管BOX・保管資材スキャン画面）関連
import com.example.buzaimanagementsystem.ui.theme.skl0101g01.SKL0101G01Screen
import com.example.buzaimanagementsystem.ui.theme.skl0101g01.SKL0101G01ApiService
import com.example.buzaimanagementsystem.ui.theme.skl0101g01.SKL0101G01ViewModel as SKL0101ViewModel

import com.example.buzaimanagementsystem.utils.CommonApiService
import com.example.buzaimanagementsystem.utils.FileLogger
import com.example.buzaimanagementsystem.utils.performLogoutAndExit

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MainActivity : ComponentActivity() {

    companion object {
        private const val TAG = "MainActivity"
    }

    /**
     * 内部共通ログ出力用（Logcat ＆ テキストファイルへ同時出力）
     */
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
        writeLog("D", "onCreate: MainActivity が起動しました")

        // Retrofit共通設定
        val retrofit = Retrofit.Builder()
            .baseUrl(BuildConfig.SERVER_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val apiService = retrofit.create(SKL0001G01ApiService::class.java)
        val commonApiService = retrofit.create(CommonApiService::class.java)

        // ログイン画面用ViewModelの生成
        val viewModel = SKL0001G01ViewModel(application, apiService)

        setContent {
            BuzaiManagementSystemTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    var currentScreen by remember { mutableStateOf("login") }

                    // セッション保持用パラメータ
                    var loginUserCd by remember { mutableStateOf("") }
                    var loginUserName by remember { mutableStateOf("") }
                    var gymTntCd by remember { mutableStateOf("") }
                    var gymTntMei by remember { mutableStateOf("") }
                    var selectedSourceDiv by remember { mutableStateOf("1") }

                    // 画面状態に応じたルーティング
                    when (currentScreen) {
                        "login" -> {
                            SKL0001G01Screen(
                                viewModel = viewModel,
                                onCloseApp = {
                                    writeLog("I", "ログイン画面からアプリを終了します")
                                    finish()
                                },
                                onLoginSuccess = { userCd, userName, tntCd, tntMei ->
                                    writeLog("I", "ログイン成功: userCd=$userCd, name=$userName, 業務担当=($tntCd:$tntMei)")
                                    loginUserCd = userCd
                                    loginUserName = userName
                                    gymTntCd = tntCd
                                    gymTntMei = tntMei
                                    currentScreen = "menu"
                                }
                            )
                        }

                        "menu" -> {
                            val menuViewModel: SKL0002ViewModel = viewModel()

                            LaunchedEffect(loginUserCd, gymTntCd) {
                                if (loginUserCd.isNotEmpty()) {
                                    menuViewModel.initUserData(
                                        userCd = loginUserCd,
                                        userName = loginUserName,
                                        gymTntCd = gymTntCd,
                                        gymTntMei = gymTntMei
                                    )
                                }
                            }

                            SKL0002G01Screen(
                                viewModel = menuViewModel,
                                userCd = loginUserCd,
                                userName = loginUserName,
                                gymTntCd = gymTntCd,
                                gymTntMei = gymTntMei,
                                onNavigateToShiwake = { _, _, _ ->
                                    writeLog("D", "メニュー -> 仕分けメニュー画面へ遷移します")
                                    currentScreen = "shiwake_menu"
                                },
                                onNavigateToShuyaku = { writeLog("D", "集約画面は未実装です") },
                                onNavigateToHikiwatashi = { writeLog("D", "引渡し画面は未実装です") },
                                onNavigateToHenkyaku = { writeLog("D", "返却品処理画面は未実装です") },
                                onNavigateToKobetsu = { writeLog("D", "その他個別作業画面は未実装です") },
                                onExitApp = {
                                    writeLog("I", "ログアウトおよびアプリ終了処理を実行します")
                                    performLogoutAndExit(
                                        coroutineScope = lifecycleScope,
                                        apiService = commonApiService,
                                        userCd = viewModel.loggedInUserCd,
                                        onExit = {
                                            finish()
                                        }
                                    )
                                }
                            )
                        }

                        "shiwake_menu" -> {
                            val shiwakeViewModel: SKL0100ViewModel = viewModel()
                            SKL0100G01Screen(
                                viewModel = shiwakeViewModel,
                                userCd = loginUserCd,
                                gymTntCd = gymTntCd,
                                gymTntMei = gymTntMei,
                                onNavigateToMain = { _, _, _ ->
                                    writeLog("D", "仕分けメニュー -> メインメニュー画面へ戻ります")
                                    currentScreen = "menu"
                                },
                                onNavigateToKobeTsukuba = { _, _, _, sourceDiv ->
                                    writeLog("D", "仕分けメニュー(神戸・つくば) -> 仕分け処理画面へ遷移 (sourceDiv=$sourceDiv)")
                                    selectedSourceDiv = sourceDiv
                                    currentScreen = "shiwake"
                                },
                                onNavigateToSuzuka = { _, _, _, sourceDiv ->
                                    writeLog("D", "仕分けメニュー(鈴鹿) -> 仕分け処理画面へ遷移 (sourceDiv=$sourceDiv)")
                                    selectedSourceDiv = sourceDiv
                                    currentScreen = "shiwake"
                                },
                                onNavigateToKatsueiWarehouse = { _, _, _, sourceDiv ->
                                    writeLog("D", "仕分けメニュー(勝営倉庫) -> 仕分け処理画面へ遷移 (sourceDiv=$sourceDiv)")
                                    selectedSourceDiv = sourceDiv
                                    currentScreen = "shiwake"
                                }
                            )
                        }

                        "shiwake" -> {
                            val skl0101ApiService = retrofit.create(SKL0101G01ApiService::class.java)
                            // ▼ application を渡すように修正
                            val skl0101ViewModel = remember { SKL0101ViewModel(application, skl0101ApiService) }

                            SKL0101G01Screen(
                                viewModel = skl0101ViewModel,
                                userCd = loginUserCd,
                                gymTntCd = gymTntCd,
                                gymTntMei = gymTntMei,
                                sourceDivision = selectedSourceDiv,
                                onNavigateToMain = { _, _, _ ->
                                    writeLog("D", "仕分け画面 -> メインメニュー画面へ戻ります")
                                    currentScreen = "menu"
                                },
                                onNavigateToMenu = { _, _, _ ->
                                    writeLog("D", "仕分け画面 -> 仕分けメニュー画面へ戻ります")
                                    currentScreen = "shiwake_menu"
                                },
                                onNavigateToShitei = { _, _, _, _, _, _, _, _ ->
                                    writeLog("D", "仕分け画面 -> 指定画面へ遷移します")
                                    // 次画面への遷移処理
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        writeLog("D", "onDestroy: MainActivity が破棄されました")
    }
}