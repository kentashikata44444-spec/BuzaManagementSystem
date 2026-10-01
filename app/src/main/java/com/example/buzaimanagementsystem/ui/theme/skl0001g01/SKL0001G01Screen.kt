package com.example.buzaimanagementsystem.ui.theme.skl0001g01

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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

// 下半分に埋め込むカメラビューのインポート
import com.example.buzaimanagementsystem.OcrCameraView

@Composable
fun SKL0001G01Screen(
    viewModel: SKL0001G01ViewModel = viewModel(),
    onCloseApp: () -> Unit,
    onLoginSuccess: (userCd: String, userName: String, gymTntCd: String, gymTntMei: String) -> Unit
) {
    var userCd by remember { mutableStateOf("") }
    // var isManualInput by remember { mutableStateOf(false) } // 【手入力復活時用】手動入力切替フラグ
    var isCameraActive by remember { mutableStateOf(false) } // カメラ起動中フラグ
    val scrollState = rememberScrollState()

    val context = LocalContext.current

    // カメラの実行時権限をリクエストするランチャー
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            if (viewModel.isSystemAvailable) {
                isCameraActive = true
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.checkSystemAvailability()
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // === 【上半分〜全体】：通常のログイン画面UI ===
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(if (isCameraActive) 0.5f else 1f)
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

                    /* =========================================================
                     * 【手入力機能（一時コメントアウト中）】
                     * 将来的に手入力を再開する場合は、このコメントアウトを外し、
                     * 下部のカメラ用Boxをコメントアウト（または切り替え）してください。
                     * =========================================================
                    if (isManualInput) {
                        OutlinedTextField(
                            value = userCd,
                            onValueChange = { userCd = it },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    if (!viewModel.isSystemAvailable) {
                                        return@KeyboardActions
                                    }

                                    val inputCodeOnly = if (userCd.contains("_")) {
                                        userCd.substringBefore("_")
                                    } else {
                                        userCd
                                    }
                                    userCd = inputCodeOnly

                                    viewModel.executeLogin(inputCodeOnly) { userCd, userName, gymTntCd, gymTntMei ->
                                        onLoginSuccess(userCd, userName, gymTntCd, gymTntMei)
                                    }
                                }
                            ),
                            modifier = Modifier.width(200.dp)
                        )
                    } else {
                    ========================================================= */

                    // 通常時のバーコード読込用ボックス（タップでカメラ起動）
                    Box(
                        modifier = Modifier
                            .width(200.dp)
                            .height(56.dp)
                            .border(1.dp, MaterialTheme.colorScheme.primary, shape = MaterialTheme.shapes.small)
                            .clickable {
                                if (!viewModel.isSystemAvailable) {
                                    return@clickable
                                }

                                when {
                                    ContextCompat.checkSelfPermission(
                                        context,
                                        Manifest.permission.CAMERA
                                    ) == PackageManager.PERMISSION_GRANTED -> {
                                        isCameraActive = true
                                    }
                                    else -> {
                                        cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                                    }
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (userCd.isEmpty()) "" else userCd,
                            fontSize = 16.sp,
                            color = if (userCd.isEmpty()) Color.Gray else Color.Black
                        )
                    }

                    /* =========================================================
                    } // <-- isManualInput の閉じ括弧 (コメントアウト中)
                    ========================================================= */
                }

                /* =========================================================
                 * 【緊急時用（手動入力切替）ボタン（一時コメントアウト中）】
                TextButton(
                    onClick = {
                        isManualInput = !isManualInput
                        isCameraActive = false
                    },
                    modifier = Modifier.padding(bottom = 24.dp)
                ) {
                    Text(
                        text = if (isManualInput) "カメラ読込に戻す" else "緊急時用（手動入力）",
                        fontSize = 14.sp
                    )
                }
                ========================================================= */

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

            // === 【下半分】：カメラ起動時のみ出現するエリア ===
            if (isCameraActive) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(0.5f)
                ) {
                    OcrCameraView(
                        onTextScanned = { scannedText ->
                            if (!viewModel.isSystemAvailable) {
                                isCameraActive = false
                                return@OcrCameraView
                            }

                            val userCodeOnly = if (scannedText.contains("_")) {
                                scannedText.substringBefore("_")
                            } else {
                                scannedText
                            }

                            isCameraActive = false
                            userCd = userCodeOnly

                            // カメラ読込によるログイン実行
                            viewModel.executeLogin(userCodeOnly) { userCd, userName, gymTntCd, gymTntMei ->
                                onLoginSuccess(userCd, userName, gymTntCd, gymTntMei)
                            }
                        }
                    )

                    Button(
                        onClick = { isCameraActive = false },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                    ) {
                        Text("閉じる")
                    }
                }
            }
        }
    }
}