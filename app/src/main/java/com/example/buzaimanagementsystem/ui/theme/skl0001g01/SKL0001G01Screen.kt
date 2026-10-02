package com.example.buzaimanagementsystem.ui.theme.skl0001g01

import android.Manifest
import android.content.pm.PackageManager
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

    // カメラ権限が許可されているかどうかの状態
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }

    // カメラの実行時権限をリクエストするランチャー
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
    }

    LaunchedEffect(Unit) {
        viewModel.checkSystemAvailability()
        if (!hasCameraPermission) {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            // === ① 裏で常にカメラを起動しておく（画面には表示されないようにサイズを 0 にするか透明にする） ===
            if (hasCameraPermission && viewModel.isSystemAvailable) {
                Box(
                    modifier = Modifier
                        .size(1.dp) // ほぼ見えないサイズで常時バックグラウンド稼働
                ) {
                    OcrCameraView(
                        onTextScanned = { scannedText ->
                            if (!viewModel.isSystemAvailable) return@OcrCameraView

                            val userCodeOnly = if (scannedText.contains("_")) {
                                scannedText.substringBefore("_")
                            } else {
                                scannedText
                            }

                            userCd = userCodeOnly

                            // カメラ読込によるログイン実行
                            viewModel.executeLogin(userCodeOnly) { uCd, userName, gymTntCd, gymTntMei ->
                                onLoginSuccess(uCd, userName, gymTntCd, gymTntMei)
                            }
                        }
                    )
                }
            }

            // === ② 通常のログイン画面UI ===
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // タイトル
                Text(
                    text = "部材所在管理システム",
                    fontSize = 24.sp,
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                // メッセージボックス
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp)
                        .padding(bottom = 24.dp)
                        .border(1.dp, Color.Gray, shape = MaterialTheme.shapes.small),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = viewModel.errorMessage ?: "ユーザーIDのバーコードを読み込んでください",
                        fontSize = 14.sp,
                        color = if (viewModel.errorMessage != null) MaterialTheme.colorScheme.error else Color.Black
                    )
                }

                // ユーザーID入力欄
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "ユーザーID",
                        fontSize = 16.sp,
                        modifier = Modifier.padding(end = 16.dp)
                    )

                    // バーコード読込結果表示用のBox（タップ不要で自動入力されます）
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

                // 閉じるボタン
                Button(
                    onClick = onCloseApp,
                    modifier = Modifier
                        .width(130.dp)
                        .height(48.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(text = "閉じる", fontSize = 16.sp)
                }

                Spacer(modifier = Modifier.height(24.dp))

                // 利用可能時間
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "利用可能時間",
                        fontSize = 20.sp,
                        modifier = Modifier.padding(end = 16.dp)
                    )
                    Text(
                        text = viewModel.displayTime,
                        fontSize = 20.sp
                    )
                }
            }
        }
    }
}