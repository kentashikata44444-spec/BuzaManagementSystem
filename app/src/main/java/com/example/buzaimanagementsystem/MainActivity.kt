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

// SKL0104G01（仕分け：鈴鹿支給品現品確定画面）関連
import com.example.buzaimanagementsystem.ui.theme.skl0104g01.SKL0104G01Screen
import com.example.buzaimanagementsystem.ui.theme.skl0104g01.SKL0104G01ApiService
import com.example.buzaimanagementsystem.ui.theme.skl0110g01.SKL0110G01ApiService
import com.example.buzaimanagementsystem.ui.theme.skl0104g01.SKL0104G01ViewModel as SKL0104ViewModel

// SKL0110G01（一時保管/保管エリア確認画面）関連
import com.example.buzaimanagementsystem.ui.theme.skl0110g01.SKL0110G01Screen
import com.example.buzaimanagementsystem.ui.theme.skl0110g01.SKL0110G01ViewModel as SKL0110ViewModel

import com.example.buzaimanagementsystem.utils.CommonApiService
import com.example.buzaimanagementsystem.utils.FileLogger
import com.example.buzaimanagementsystem.utils.performLogoutAndExit
import com.example.buzaimanagementsystem.utils.BluetoothConnectionHelper

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    companion object {
        private const val TAG = "MainActivity"
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
        writeLog("D", "onCreate: MainActivity が起動しました")

        val serverUrl = BuildConfig.SERVER_URL
        writeLog("I", "onCreate: 設定された通信先サーバーURL (BuildConfig.SERVER_URL) = $serverUrl")

        val retrofit = Retrofit.Builder()
            .baseUrl(serverUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val apiService = retrofit.create(SKL0001G01ApiService::class.java)
        val commonApiService = retrofit.create(CommonApiService::class.java)

        val viewModel = SKL0001G01ViewModel(application, apiService)

        setContent {
            BuzaiManagementSystemTheme {
                // === アプリ起動時にBLEデバイスの接続をバックグラウンドで1度だけ試行する ===
                LaunchedEffect(Unit) {
                    val context = applicationContext
                    val savedAddress = BluetoothConnectionHelper.getSavedBleDeviceAddress(context)
                    if (!savedAddress.isNullOrEmpty()) {
                        writeLog("I", "アプリ起動時: 保存されたBLEアドレス ($savedAddress) のバックグラウンド接続を開始します")

                        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
                            if (!BluetoothConnectionHelper.isBleConnected(context, savedAddress)) {
                                BluetoothConnectionHelper.connectToSavedDevice(
                                    context = context,
                                    onConnectionStateChanged = { isConnected, address ->
                                        writeLog("I", "アプリ起動時バックグラウンド接続コールバック: isConnected=$isConnected, address=$address")
                                    },
                                    onDetailedError = { detailedMessage ->
                                        writeLog("W", "アプリ起動時BLEエラー: $detailedMessage")
                                    }
                                )
                            } else {
                                writeLog("I", "アプリ起動時: すでにBLE機器は接続されています")
                            }
                        }
                    } else {
                        writeLog("I", "アプリ起動時: 保存されたBLEアドレスはありません")
                    }
                }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    var currentScreen by remember { mutableStateOf("login") }

                    var loginUserCd by remember { mutableStateOf("") }
                    var loginUserName by remember { mutableStateOf("") }
                    var gymTntCd by remember { mutableStateOf("") }
                    var gymTntMei by remember { mutableStateOf("") }
                    var selectedSourceDiv by remember { mutableStateOf("1") }

                    // SKL0101から引き継ぐ選択値用の状態
                    var selectedChubunruiCode by remember { mutableStateOf("") }
                    var selectedChubunruiName by remember { mutableStateOf("") }
                    var selectedShobunruiCode by remember { mutableStateOf("") }
                    var selectedShobunruiName by remember { mutableStateOf("") }

                    // SKL0104からSKL0110へ引き継ぐ製品No
                    var selectedSeihinBango by remember { mutableStateOf("") }

                    when (currentScreen) {
                        "login" -> {
                            SKL0001G01Screen(
                                viewModel = viewModel,
                                onCloseApp = { finish() },
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
                                onNavigateToShiwake = { _, _, _ ->
                                    currentScreen = "shiwake_menu"
                                },
                                onNavigateToShuyaku = {},
                                onNavigateToHikiwatashi = {},
                                onNavigateToHenkyaku = {},
                                onNavigateToKobetsu = {},
                                onExitApp = {
                                    performLogoutAndExit(
                                        coroutineScope = lifecycleScope,
                                        apiService = commonApiService,
                                        userCd = viewModel.loggedInUserCd,
                                        onExit = { finish() }
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
                                    currentScreen = "menu"
                                },
                                onNavigateToKobeTsukuba = { _, _, _, sourceDiv ->
                                    selectedSourceDiv = sourceDiv
                                    currentScreen = "shiwake"
                                },
                                onNavigateToSuzuka = { _, _, _, sourceDiv ->
                                    selectedSourceDiv = sourceDiv
                                    currentScreen = "shiwake"
                                },
                                onNavigateToKatsueiWarehouse = { _, _, _, sourceDiv ->
                                    selectedSourceDiv = sourceDiv
                                    currentScreen = "shiwake"
                                }
                            )
                        }

                        "shiwake" -> {
                            var subScreen by remember { mutableStateOf("scan") }

                            when (subScreen) {
                                "skl0110" -> {
                                    val skl0110ApiService = retrofit.create(SKL0110G01ApiService::class.java)
                                    val skl0110ViewModel = remember(loginUserCd, gymTntCd, gymTntMei, selectedSourceDiv, selectedSeihinBango) {
                                        SKL0110ViewModel(application, skl0110ApiService).apply {
                                            initParams(
                                                userCd = loginUserCd,
                                                gymTntCd = gymTntCd,
                                                gymTntMei = gymTntMei,
                                                sourceDivision = selectedSourceDiv,
                                                seihinBango = selectedSeihinBango
                                            )
                                        }
                                    }

                                    SKL0110G01Screen(
                                        viewModel = skl0110ViewModel,
                                        userCd = loginUserCd,
                                        gymTntCd = gymTntCd,
                                        gymTntMei = gymTntMei,
                                        sourceDivision = selectedSourceDiv,
                                        seihinBango = selectedSeihinBango,
                                        onNavigateToMenu = {
                                            currentScreen = "shiwake_menu"
                                        },
                                        onNavigateToChange = { // ← onNavigateToAreaChange から変更
                                            subScreen = "skl0104"
                                        },
                                        onNavigateToTemporary = { // ← onNavigateToHokanAreaKakutei から変更
                                            currentScreen = "menu"
                                        }
                                    )
                                }

                                "skl0104" -> {
                                    val skl0104ApiService = retrofit.create(SKL0104G01ApiService::class.java)
                                    val skl0104ViewModel = remember(
                                        loginUserCd, gymTntCd, gymTntMei, selectedSourceDiv,
                                        selectedChubunruiCode, selectedChubunruiName,
                                        selectedShobunruiCode, selectedShobunruiName
                                    ) {
                                        SKL0104ViewModel(application, skl0104ApiService).apply {
                                            initParams(
                                                userCd = loginUserCd,
                                                gymTntCd = gymTntCd,
                                                gymTntMei = gymTntMei,
                                                sourceDivision = selectedSourceDiv,
                                                chuCode = selectedChubunruiCode,
                                                chuName = selectedChubunruiName,
                                                shoCode = selectedShobunruiCode,
                                                shoName = selectedShobunruiName
                                            )
                                        }
                                    }

                                    SKL0104G01Screen(
                                        viewModel = skl0104ViewModel,
                                        userCd = loginUserCd,
                                        gymTntCd = gymTntCd,
                                        gymTntMei = gymTntMei,
                                        sourceDivision = selectedSourceDiv,
                                        chuCode = selectedChubunruiCode,
                                        chuName = selectedChubunruiName,
                                        shoCode = selectedShobunruiCode,
                                        shoName = selectedShobunruiName,
                                        onNavigateToMain = { _, _, _ -> currentScreen = "menu" },
                                        onNavigateToMenu = { _, _, _ ->
                                            writeLog("D", "仕分け・一時保管メニューへ戻る (shiwake_menu)")
                                            currentScreen = "shiwake_menu"
                                        },
                                        onNavigateToChange = { _, _, _, _ ->
                                            writeLog("D", "保管BOX・保管資材変更へ遷移 (SKL0101に戻る)")
                                            subScreen = "scan"
                                        },
                                        onNavigateToTemporary = { _, _, _, _, sb ->
                                            writeLog("D", "一時保管(保管エリア確定)画面へ遷移: seihinBango=$sb")
                                            selectedSeihinBango = sb
                                            subScreen = "skl0110"
                                        },
                                        onNavigateToEmergency = { _, _, _ -> }
                                    )
                                }

                                else -> {
                                    val skl0101ApiService = retrofit.create(SKL0101G01ApiService::class.java)
                                    val skl0101ViewModel = remember { SKL0101ViewModel(application, skl0101ApiService) }

                                    SKL0101G01Screen(
                                        viewModel = skl0101ViewModel,
                                        userCd = loginUserCd,
                                        gymTntCd = gymTntCd,
                                        gymTntMei = gymTntMei,
                                        sourceDivision = selectedSourceDiv,
                                        onNavigateToMain = { _, _, _ -> currentScreen = "menu" },
                                        onNavigateToMenu = { _, _, _ -> currentScreen = "shiwake_menu" },
                                        onNavigateToShitei = { uCd, tCd, tMei, cCode, cName, sCode, sName, sDiv ->
                                            writeLog("D", "仕分け画面 -> 次の画面へ遷移 (sourceDiv=$sDiv, cCode=$cCode, sCode=$sCode)")
                                            selectedSourceDiv = sDiv
                                            selectedChubunruiCode = cCode
                                            selectedChubunruiName = cName
                                            selectedShobunruiCode = sCode
                                            selectedShobunruiName = sName

                                            if (sDiv == "2") {
                                                subScreen = "skl0104"
                                            } else {
                                                // TODO: 区分1や3の場合の遷移処理
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}