package com.example.buzaimanagementsystem.ui.theme.skl0001g01

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.buzaimanagementsystem.OcrCameraView

@Composable
fun SKL0001G01Screen(
    viewModel: SKL0001G01ViewModel = viewModel(),
    onCloseApp: () -> Unit,
    onLoginSuccess: (userCd: String, userName: String, gymTntCd: String, gymTntMei: String) -> Unit
) {
    var userCd by remember { mutableStateOf("") }
    val scrollState = rememberScrollState()
    val context = LocalContext.current

    var lastScannedText by remember { mutableStateOf<String?>(null) }
    var lastScannedTime by remember { mutableStateOf(0L) }

    // Android 12以降の判定と必要な権限リストの作成
    val isAndroid12Plus = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    val requiredPermissions = remember {
        mutableListOf(Manifest.permission.CAMERA).apply {
            if (isAndroid12Plus) {
                add(Manifest.permission.BLUETOOTH_SCAN)
                add(Manifest.permission.BLUETOOTH_CONNECT)
            }
        }.toTypedArray()
    }

    // すべての権限が許可されているかチェック
    var hasAllPermissions by remember {
        mutableStateOf(
            requiredPermissions.all {
                ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
            }
        )
    }

    // 複数権限を同時にリクエストするランチャー
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        hasAllPermissions = permissions.values.all { it }
    }

    LaunchedEffect(Unit) {
        viewModel.checkSystemAvailability()
        if (!hasAllPermissions) {
            permissionLauncher.launch(requiredPermissions)
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            // すべての権限（カメラ＋Bluetooth）が許可されている場合にカメラを起動
            if (hasAllPermissions && viewModel.isSystemAvailable) {
                Box(modifier = Modifier.size(1.dp)) {
                    OcrCameraView(
                        onTextScanned = { scannedText ->
                            if (!viewModel.isSystemAvailable || viewModel.isLoggingIn) return@OcrCameraView

                            val currentTime = System.currentTimeMillis()
                            if (scannedText == lastScannedText && (currentTime - lastScannedTime) < 3000) {
                                return@OcrCameraView
                            }

                            lastScannedText = scannedText
                            lastScannedTime = currentTime

                            val inputUser = scannedText.split("_", limit = 2)[0].trim()
                            userCd = inputUser

                            viewModel.executeLogin(scannedText) { uCd, userName, gymTntCd, gymTntMei ->
                                onLoginSuccess(uCd, userName, gymTntCd, gymTntMei)
                            }
                        }
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "部材所在管理システム",
                    fontSize = 24.sp,
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp)
                        .padding(bottom = 24.dp)
                        .border(1.dp, Color.Gray, shape = MaterialTheme.shapes.small),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = viewModel.errorMessage ?: "ユーザーIDのQRコードを読み込んでください",
                        fontSize = 14.sp,
                        color = if (viewModel.errorMessage != null) MaterialTheme.colorScheme.error else Color.Black
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(text = "ユーザーID", fontSize = 16.sp, modifier = Modifier.padding(end = 16.dp))

                    Box(
                        modifier = Modifier
                            .width(200.dp)
                            .height(56.dp)
                            .background(Color(0xFFD4E4F7), shape = MaterialTheme.shapes.small)
                            .border(1.dp, Color.Black, shape = MaterialTheme.shapes.small)
                            .padding(horizontal = 12.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Text(
                            text = userCd,
                            fontSize = 16.sp,
                            color = if (userCd.isEmpty()) Color.Gray else Color.Black,
                            maxLines = 1
                        )
                    }
                }

                Button(
                    onClick = onCloseApp,
                    modifier = Modifier.width(130.dp).height(48.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(text = "閉じる", fontSize = 16.sp)
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "利用可能時間", fontSize = 20.sp, modifier = Modifier.padding(end = 16.dp))
                    Text(text = viewModel.displayTime, fontSize = 20.sp)
                }
            }
        }
    }
}