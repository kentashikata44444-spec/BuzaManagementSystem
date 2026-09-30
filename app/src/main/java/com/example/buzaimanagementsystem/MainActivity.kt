package com.example.buzaimanagementsystem

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.lifecycleScope
import com.example.buzaimanagementsystem.ui.theme.BuzaiManagementSystemTheme

import com.example.buzaimanagementsystem.ui.theme.skl0001g01.SKL0001G01Screen
import com.example.buzaimanagementsystem.ui.theme.skl0001g01.SKL0001G01ApiService
import com.example.buzaimanagementsystem.ui.theme.skl0001g01.SKL0001G01ViewModel

import com.example.buzaimanagementsystem.ui.theme.skl0002g01.SKL0002G01Screen
import com.example.buzaimanagementsystem.ui.theme.skl0002g01.SKL0002G01ViewModel as SKL0002ViewModel

// ▼ 仕分け・一時保管画面用のインポートを追加してください
import com.example.buzaimanagementsystem.ui.theme.skl0100g01.SKL0100G01Screen
import com.example.buzaimanagementsystem.ui.theme.skl0100g01.SKL0100G01ViewModel as SKL0100ViewModel

import com.example.buzaimanagementsystem.utils.CommonApiService
import com.example.buzaimanagementsystem.utils.performLogoutAndExit

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val retrofit = Retrofit.Builder()
            .baseUrl(BuildConfig.SERVER_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val apiService = retrofit.create(SKL0001G01ApiService::class.java)
        val commonApiService = retrofit.create(CommonApiService::class.java)
        val viewModel = SKL0001G01ViewModel(apiService)

        setContent {
            BuzaiManagementSystemTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    var currentScreen by remember { mutableStateOf("login") }

                    // 保持するユーザー・担当情報
                    var loginUserCd by remember { mutableStateOf("") }
                    var loginUserName by remember { mutableStateOf("") }
                    var gymTntCd by remember { mutableStateOf("") }
                    var gymTntMei by remember { mutableStateOf("") }

                    when (currentScreen) {
                        "login" -> {
                            SKL0001G01Screen(
                                viewModel = viewModel,
                                onCloseApp = {
                                    finish()
                                },
                                onLoginSuccess = { userCd, userName, tntCd, tntMei ->
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
                                onNavigateToShiwake = { uCd, tCd, tMei ->
                                    // ▼ ここで仕分け画面へ切り替えるステートに変更
                                    currentScreen = "shiwake"
                                },
                                onNavigateToShuyaku = { /* 集約画面への遷移 */ },
                                onNavigateToHikiwatashi = { /* 引渡し画面への遷移 */ },
                                onNavigateToHenkyaku = { /* 返却品処理画面への遷移 */ },
                                onNavigateToKobetsu = { /* その他個別作業画面への遷移 */ },
                                onExitApp = {
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

                        // ▼ SKL0100G01（仕分け・一時保管メニュー）への遷移先を追加
                        "shiwake" -> {
                            val shiwakeViewModel: SKL0100ViewModel = viewModel()

                            SKL0100G01Screen(
                                viewModel = shiwakeViewModel,
                                userCd = loginUserCd,
                                gymTntCd = gymTntCd,
                                gymTntMei = gymTntMei,
                                onNavigateToMain = { uCd, tCd, tMei ->
                                    // メインメニューに戻る
                                    currentScreen = "menu"
                                },
                                onNavigateToKobeTsukuba = { uCd, tCd, tMei, sourceDiv ->
                                    // 神戸・筑波支給品画面への遷移処理
                                },
                                onNavigateToSuzuka = { uCd, tCd, tMei, sourceDiv ->
                                    // 鈴鹿支給品画面への遷移処理
                                },
                                onNavigateToKatsueiWarehouse = { uCd, tCd, tMei, sourceDiv ->
                                    // 勝英購入品・倉庫品画面への遷移処理
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}