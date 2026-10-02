package com.example.buzaimanagementsystem.ui.theme.skl0101g01

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.buzaimanagementsystem.OcrCameraView

import com.example.buzaimanagementsystem.R

@Composable
fun SKL0101G01Screen(
    viewModel: SKL0101G01ViewModel,
    userCd: String,
    gymTntCd: String,
    gymTntMei: String,
    sourceDivision: String = "1",
    onNavigateToMain: (userCd: String, gymTntCd: String, gymTntMei: String) -> Unit,
    onNavigateToMenu: (userCd: String, gymTntCd: String, gymTntMei: String) -> Unit,
    onNavigateToShitei: (userCd: String, gymTntCd: String, gymTntMei: String, chuCode: String, chuName: String, shoCode: String, shoName: String, sourceDiv: String) -> Unit
) {
    val context = LocalContext.current

    // モーダルの表示・非表示を管理するステート
    var showHelpDialog by remember { mutableStateOf(false) }

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
    }

    LaunchedEffect(userCd, gymTntCd, gymTntMei, sourceDivision) {
        viewModel.initParams(userCd, gymTntCd, gymTntMei, sourceDivision)
        viewModel.clearInput()
        if (!hasCameraPermission) {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(16.dp)
        ) {
            // === 裏で常にカメラを起動して自動スキャンさせる ===
            if (hasCameraPermission) {
                Box(
                    modifier = Modifier.size(1.dp)
                ) {
                    OcrCameraView(
                        onTextScanned = { scannedText ->
                            viewModel.processScannedCode(scannedText) { chuCode, chuName, shoCode, shoName ->
                                onNavigateToShitei(userCd, gymTntCd, gymTntMei, chuCode, chuName, shoCode, shoName, sourceDivision)
                            }
                        }
                    )
                }
            }

            // === 通常の仕分け画面UI ===
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.Start
            ) {
                // 1段目：業務担当名（右寄せ）
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Text(
                        text = gymTntMei.ifEmpty { "業務担当名" },
                        fontSize = 14.sp,
                        color = Color.Black
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // 2段目：タイトル
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "仕分け",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    Text(
                        text = "(保管BOX・保管資材確定)",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 3段目：仕分け・一時保管メニューボタン（右寄せ・グレー＆黒枠）
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(
                        onClick = {
                            onNavigateToMenu(userCd, gymTntCd, gymTntMei)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE0E0E0)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.border(1.dp, Color.Black, RoundedCornerShape(8.dp))
                    ) {
                        Text(
                            text = "仕分け・一時保管メニュー",
                            color = Color.Black,
                            fontSize = 14.sp
                        )
                    }
                }

                // エラーメッセージ表示エリア
                if (viewModel.errorMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFFFCDD2), shape = RoundedCornerShape(4.dp))
                            .padding(8.dp)
                    ) {
                        Text(
                            text = viewModel.errorMessage!!,
                            color = Color.Red,
                            fontSize = 13.sp
                        )
                    }
                }

                // インフォメッセージ表示エリア
                if (viewModel.infoMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFE8F5E9), shape = RoundedCornerShape(4.dp))
                            .padding(8.dp)
                    ) {
                        Text(
                            text = viewModel.infoMessage!!,
                            color = Color(0xFF2E7D32),
                            fontSize = 13.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Text(
                            text = "※『保管BOX』または『保管資材』の\n バーコードを読み込んでください",
                            fontSize = 14.sp,
                            color = Color.Black,
                            textAlign = TextAlign.Start
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // 保管BOX入力欄
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "保管BOX",
                            fontSize = 16.sp,
                            modifier = Modifier.width(90.dp),
                            color = Color.Black
                        )

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .background(Color(0xFFD4E4F7), shape = RoundedCornerShape(4.dp))
                                .border(1.dp, Color.Black, shape = RoundedCornerShape(4.dp))
                                .padding(horizontal = 12.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Text(
                                text = viewModel.hokanBoxCode,
                                fontSize = 16.sp,
                                color = if (viewModel.hokanBoxCode.isEmpty()) Color.Gray else Color.Black,
                                maxLines = 1
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "または", fontSize = 14.sp, color = Color.Black)
                    }

                    // 保管資材入力欄
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier.width(90.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "保管資材", fontSize = 16.sp, color = Color.Black)
                            // ▼ 「？」ボタンをタップしたらモーダルを表示するよう変更
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .background(Color.Black, shape = RoundedCornerShape(10.dp))
                                    .clickable { showHelpDialog = true },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = "?", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .background(Color(0xFFD4E4F7), shape = RoundedCornerShape(4.dp))
                                .border(1.dp, Color.Black, shape = RoundedCornerShape(4.dp))
                                .padding(horizontal = 12.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Text(
                                text = viewModel.hokanShizaiCode,
                                fontSize = 16.sp,
                                color = if (viewModel.hokanShizaiCode.isEmpty()) Color.Gray else Color.Black,
                                maxLines = 1
                            )
                        }
                    }
                }
            }

            // === 保管資材説明モーダル（AlertDialog） ===
            if (showHelpDialog) {
                AlertDialog(
                    onDismissRequest = { showHelpDialog = false },
                    containerColor = Color.White,
                    shape = RoundedCornerShape(8.dp),
                    title = {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "保管資材",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                            Button(
                                onClick = { showHelpDialog = false },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE0E0E0)),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.border(1.dp, Color.Black, RoundedCornerShape(8.dp))
                            ) {
                                Text(text = "閉じる", color = Color.Black, fontSize = 14.sp)
                            }
                        }
                    },
                    text = {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(450.dp)
                                .verticalScroll(rememberScrollState())
                                .padding(vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // コンビテナ
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(text = "コンビテナ", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                                Spacer(modifier = Modifier.height(4.dp))
                                Image(
                                    painter = painterResource(id = R.drawable.conbitena),
                                    contentDescription = "コンビテナ",
                                    modifier = Modifier.size(150.dp)
                                )
                            }

                            // パレット
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(text = "パレット", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                                Spacer(modifier = Modifier.height(4.dp))
                                Image(
                                    painter = painterResource(id = R.drawable.palette),
                                    contentDescription = "パレット",
                                    modifier = Modifier.size(150.dp)
                                )
                            }

                            // 台車（2種類あるため横並びか縦並びに配置）
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(text = "台車", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Image(
                                        painter = painterResource(id = R.drawable.daisha1),
                                        contentDescription = "台車1",
                                        modifier = Modifier.size(120.dp)
                                    )
                                    Image(
                                        painter = painterResource(id = R.drawable.daisha2),
                                        contentDescription = "台車2",
                                        modifier = Modifier.size(120.dp)
                                    )
                                }
                            }

                            // 棚
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(text = "棚", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                                Spacer(modifier = Modifier.height(4.dp))
                                Image(
                                    painter = painterResource(id = R.drawable.tana),
                                    contentDescription = "棚",
                                    modifier = Modifier.size(150.dp)
                                )
                            }
                        }
                    },
                    confirmButton = {}
                )
            }
        }
    }
}