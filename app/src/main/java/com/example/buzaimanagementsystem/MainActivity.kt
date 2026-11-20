package com.example.buzaimanagementsystem

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.buzaimanagementsystem.ui.theme.BuzaiManagementSystemTheme
import com.example.buzaimanagementsystem.ui.theme.skl0001g01.SKL0001G01ApiService
import com.example.buzaimanagementsystem.ui.theme.skl0001g01.SKL0001G01Screen
import com.example.buzaimanagementsystem.ui.theme.skl0001g01.SKL0001G01ViewModel
import com.example.buzaimanagementsystem.ui.theme.skl0002g01.SKL0002G01Screen
import com.example.buzaimanagementsystem.ui.theme.skl0002g01.SKL0002G01ViewModel as SKL0002ViewModel
import com.example.buzaimanagementsystem.ui.theme.skl0100g01.SKL0100G01Screen
import com.example.buzaimanagementsystem.ui.theme.skl0100g01.SKL0100G01ViewModel as SKL0100ViewModel
import com.example.buzaimanagementsystem.ui.theme.skl0101g01.SKL0101G01ApiService
import com.example.buzaimanagementsystem.ui.theme.skl0101g01.SKL0101G01Screen
import com.example.buzaimanagementsystem.ui.theme.skl0101g01.SKL0101G01ViewModel as SKL0101ViewModel
import com.example.buzaimanagementsystem.ui.theme.skl0104g01.SKL0104G01ApiService
import com.example.buzaimanagementsystem.ui.theme.skl0104g01.SKL0104G01Screen
import com.example.buzaimanagementsystem.ui.theme.skl0104g01.SKL0104G01ViewModel
import com.example.buzaimanagementsystem.ui.theme.skl0110g01.SKL0110G01ApiService
import com.example.buzaimanagementsystem.ui.theme.skl0110g01.SKL0110G01Screen
import com.example.buzaimanagementsystem.ui.theme.skl0110g01.SKL0110G01ViewModel as SKL0110ViewModel
import com.example.buzaimanagementsystem.ui.theme.skl0111g01.SKL0111G01ApiService
import com.example.buzaimanagementsystem.ui.theme.skl0111g01.SKL0111G01Screen
import com.example.buzaimanagementsystem.ui.theme.skl0111g01.SKL0111G01ViewModel as SKL0111ViewModel
import com.example.buzaimanagementsystem.utils.AppConfig
import com.example.buzaimanagementsystem.utils.CommonApiService
import com.example.buzaimanagementsystem.utils.FileLogger
import com.example.buzaimanagementsystem.utils.SwipeWarningBox
import com.example.buzaimanagementsystem.utils.performLogoutAndExit
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

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

        val rawServerUrl = AppConfig.getServerUrl(applicationContext)
        writeLog("I", "【設定ファイル調査】AppConfig.getServerUrl() の返り値 = [$rawServerUrl]")

        val serverUrl = if (rawServerUrl.isBlank()) {
            writeLog("E", "onCreate: サーバーURLの取得に失敗しました。設定ファイルの配置を確認してください。")
            "http://192.168.137.1:8080/"
        } else {
            if (rawServerUrl.endsWith("/")) rawServerUrl else "$rawServerUrl/"
        }

        writeLog("I", "onCreate: 適用された通信先サーバーURL = $serverUrl")

        val retrofit = Retrofit.Builder()
            .baseUrl(serverUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val apiService = retrofit.create(SKL0001G01ApiService::class.java)
        val commonApiService = retrofit.create(CommonApiService::class.java)

        val viewModel = SKL0001G01ViewModel(application, apiService)

        setContent {
            BuzaiManagementSystemTheme {
                SwipeWarningBox {
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

                        var selectedChubunruiCode by remember { mutableStateOf("") }
                        var selectedChubunruiName by remember { mutableStateOf("") }
                        var selectedShobunruiCode by remember { mutableStateOf("") }
                        var selectedShobunruiName by remember { mutableStateOf("") }

                        var selectedSeihinBango by remember { mutableStateOf("") }

                        key(currentScreen) {
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

                                    val skl0104ApiService = remember {
                                        retrofit.create(SKL0104G01ApiService::class.java)
                                    }
                                    val skl0104Factory = remember(application, skl0104ApiService) {
                                        object : ViewModelProvider.Factory {
                                            @Suppress("UNCHECKED_CAST")
                                            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                                                return SKL0104G01ViewModel(application, skl0104ApiService) as T
                                            }
                                        }
                                    }
                                    var skl0104ViewModel by remember {
                                        mutableStateOf<SKL0104G01ViewModel?>(null)
                                    }

                                    key(subScreen) {
                                        when (subScreen) {
                                            "skl0110" -> {
                                                val skl0110ApiService = remember {
                                                    retrofit.create(SKL0110G01ApiService::class.java)
                                                }
                                                val skl0110ViewModel = remember(
                                                    loginUserCd,
                                                    gymTntCd,
                                                    gymTntMei,
                                                    selectedSourceDiv,
                                                    selectedSeihinBango
                                                ) {
                                                    SKL0110ViewModel(application, skl0110ApiService).apply {
                                                        initParams(
                                                            userCd = loginUserCd,
                                                            gymTntCd = gymTntCd,
                                                            gymTntMei = gymTntMei,
                                                            sourceDivision = selectedSourceDiv,
                                                            seihinBango = selectedSeihinBango
                                                        )
                                                        onNavigateToNextScreen = {
                                                            writeLog(
                                                                "D",
                                                                "保管資材確定対象データが0件のため、自動で一時保管(保管エリア確定)画面へ遷移します: seihinBango=$selectedSeihinBango"
                                                            )
                                                            subScreen = "skl0111"
                                                        }
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
                                                    onNavigateToChange = {
                                                        skl0110ViewModel.clearHokanShizai()
                                                    },
                                                    onNavigateToTemporary = {
                                                        writeLog(
                                                            "D",
                                                            "一時保管(保管エリア確定)画面へ手動遷移: seihinBango=$selectedSeihinBango"
                                                        )
                                                        subScreen = "skl0111"
                                                    }
                                                )
                                            }

                                            "skl0111" -> {
                                                val skl0111ApiService = remember {
                                                    retrofit.create(SKL0111G01ApiService::class.java)
                                                }
                                                val skl0111ViewModel = remember(
                                                    loginUserCd,
                                                    gymTntCd,
                                                    gymTntMei,
                                                    selectedSourceDiv,
                                                    selectedSeihinBango
                                                ) {
                                                    SKL0111ViewModel(application, skl0111ApiService).apply {
                                                        initParams(
                                                            userCd = loginUserCd,
                                                            gymTntCd = gymTntCd,
                                                            gymTntMei = gymTntMei,
                                                            sourceDivision = selectedSourceDiv,
                                                            seihinBango = selectedSeihinBango
                                                        )
                                                    }
                                                }

                                                SKL0111G01Screen(
                                                    viewModel = skl0111ViewModel,
                                                    userCd = loginUserCd,
                                                    gymTntCd = gymTntCd,
                                                    gymTntMei = gymTntMei,
                                                    sourceDivision = selectedSourceDiv,
                                                    seihinBango = selectedSeihinBango,
                                                    onNavigateToMenu = {
                                                        skl0104ViewModel?.resetRegistrationState()
                                                        currentScreen = "shiwake_menu"
                                                    },
                                                    onNavigateToAreaChange = {
                                                        skl0111ViewModel.clearHokanArea()
                                                    }
                                                )
                                            }

                                            "skl0104" -> {
                                                val vm = viewModel<SKL0104G01ViewModel>(
                                                    key = "skl0104_shiwake",
                                                    factory = skl0104Factory
                                                ).also { skl0104ViewModel = it }

                                                LaunchedEffect(
                                                    loginUserCd, gymTntCd, gymTntMei, selectedSourceDiv,
                                                    selectedChubunruiCode, selectedChubunruiName,
                                                    selectedShobunruiCode, selectedShobunruiName
                                                ) {
                                                    vm.initParams(
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

                                                SKL0104G01Screen(
                                                    viewModel = vm,
                                                    userCd = loginUserCd,
                                                    gymTntCd = gymTntCd,
                                                    gymTntMei = gymTntMei,
                                                    sourceDivision = selectedSourceDiv,
                                                    chuCode = selectedChubunruiCode,
                                                    chuName = selectedChubunruiName,
                                                    shoCode = selectedShobunruiCode,
                                                    shoName = selectedShobunruiName,
                                                    onNavigateToMain = { _, _, _ ->
                                                        vm.clearMessages()
                                                        currentScreen = "menu"
                                                    },
                                                    onNavigateToMenu = { _, _, _ ->
                                                        writeLog(
                                                            "D",
                                                            "仕分け・一時保管メニューへ戻る (shiwake_menu)"
                                                        )
                                                        vm.clearMessages()
                                                        vm.clearInput()
                                                        currentScreen = "shiwake_menu"
                                                    },
                                                    onNavigateToChange = { _, _, _, _ ->
                                                        writeLog(
                                                            "D",
                                                            "保管BOX・保管資材変更へ遷移 (SKL0101に戻る)"
                                                        )
                                                        // 遷移前に SKL0104 側の入力・メッセージをクリアしてから画面を切り替える
                                                        vm.clearInput()
                                                        subScreen = "scan"
                                                    },
                                                    onNavigateToTemporary = { _, _, _, _, sb ->
                                                        if (!vm.canNavigateToTemporaryStorage()) {
                                                            return@SKL0104G01Screen
                                                        }
                                                        writeLog(
                                                            "D",
                                                            "一時保管(保管エリア確定)画面へ遷移: seihinBango=$sb"
                                                        )
                                                        vm.clearMessages()
                                                        selectedSeihinBango = sb
                                                        subScreen = "skl0110"
                                                    },
                                                    onNavigateToEmergency = { uCd, tCd, tMei ->
                                                        writeLog(
                                                            "D",
                                                            "緊急品集約ボタン押下 -> メインメニュー(SKL0002G01)へ遷移: userCd=$uCd, gymTntCd=$tCd, gymTntMei=$tMei"
                                                        )
                                                        vm.resetRegistrationState()
                                                        if (uCd.isNotEmpty()) loginUserCd = uCd
                                                        if (tCd.isNotEmpty()) gymTntCd = tCd
                                                        if (tMei.isNotEmpty()) gymTntMei = tMei
                                                        currentScreen = "menu"
                                                    }
                                                )
                                            }

                                            else -> {
                                                val skl0101ApiService = remember {
                                                    retrofit.create(SKL0101G01ApiService::class.java)
                                                }
                                                val skl0101Factory = remember(application, skl0101ApiService) {
                                                    object : ViewModelProvider.Factory {
                                                        @Suppress("UNCHECKED_CAST")
                                                        override fun <T : ViewModel> create(modelClass: Class<T>): T {
                                                            return SKL0101ViewModel(application, skl0101ApiService) as T
                                                        }
                                                    }
                                                }
                                                val skl0101ViewModel = viewModel<SKL0101ViewModel>(
                                                    key = "skl0101_shiwake",
                                                    factory = skl0101Factory
                                                )

                                                SKL0101G01Screen(
                                                    viewModel = skl0101ViewModel,
                                                    userCd = loginUserCd,
                                                    gymTntCd = gymTntCd,
                                                    gymTntMei = gymTntMei,
                                                    sourceDivision = selectedSourceDiv,
                                                    registeredItemCount = skl0104ViewModel?.registeredItemCount ?: 0,
                                                    onNavigateToMain = { _, _, _ -> currentScreen = "menu" },
                                                    onNavigateToMenu = { _, _, _ ->
                                                        currentScreen = "shiwake_menu"
                                                    },
                                                    onNavigateToShitei = { uCd, tCd, tMei, cCode, cName, sCode, sName, sDiv ->
                                                        writeLog(
                                                            "D",
                                                            "仕分け画面 -> 次の画面へ遷移 (sourceDiv=$sDiv, cCode=$cCode, sCode=$sCode)"
                                                        )
                                                        selectedSourceDiv = sDiv
                                                        selectedChubunruiCode = cCode
                                                        selectedChubunruiName = cName
                                                        selectedShobunruiCode = sCode
                                                        selectedShobunruiName = sName

                                                        if (sDiv == "2") {
                                                            // 遷移前に SKL0104 側の前回入力・メッセージをクリアしてから画面を切り替える
                                                            skl0104ViewModel?.clearInput()
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
        }

        hideNavigationButtons()
    }

    private fun hideNavigationButtons() {
        val window = this.window

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val controller = window.insetsController
            if (controller != null) {
                controller.hide(WindowInsets.Type.navigationBars())
                controller.systemBarsBehavior =
                    WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else {
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = (
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                            or View.SYSTEM_UI_FLAG_FULLSCREEN
                    )
        }
    }

    override fun onResume() {
        super.onResume()
        hideNavigationButtons()
    }
}