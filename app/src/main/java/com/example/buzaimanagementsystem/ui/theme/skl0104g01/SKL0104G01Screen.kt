package com.example.buzaimanagementsystem.ui.theme.skl0104g01

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
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
import com.example.buzaimanagementsystem.utils.BluetoothConnectionHelper
import com.example.buzaimanagementsystem.utils.FileLogger

private const val TAG = "SKL0104G01Screen"

@Composable
fun SKL0104G01Screen(
    viewModel: SKL0104G01ViewModel,
    userCd: String,
    gymTntCd: String,
    gymTntMei: String,
    sourceDivision: String = "1",
    chuCode: String = "",
    chuName: String = "",
    shoCode: String = "",
    shoName: String = "",
    onNavigateToMain: (userCd: String, gymTntCd: String, gymTntMei: String) -> Unit,
    onNavigateToMenu: (userCd: String, gymTntCd: String, gymTntMei: String) -> Unit,
    onNavigateToChange: (userCd: String, gymTntCd: String, gymTntMei: String, sourceDivision: String) -> Unit,
    onNavigateToTemporary: (userCd: String, gymTntCd: String, gymTntMei: String, sourceDivision: String, seihinBango: String) -> Unit,
    onNavigateToEmergency: (userCd: String, gymTntCd: String, gymTntMei: String) -> Unit
) {
    val context = LocalContext.current

    var showHelpDialog by remember { mutableStateOf(false) }
    var isCameraActive by remember { mutableStateOf(false) }

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
        if (isGranted) {
            isCameraActive = true
        }
    }

    var hasBluetoothPermission by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED &&
                        ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED
            } else {
                true
            }
        )
    }

    val bluetoothPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        hasBluetoothPermission = permissions.entries.all { it.value }
    }

    LaunchedEffect(userCd, gymTntCd, gymTntMei, sourceDivision, chuCode, chuName, shoCode, shoName) {
        FileLogger.writeLog(context, "DEBUG", TAG, "画面初期化パラメータ受信")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val needConnect = ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED
            val needScan = ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED
            if (needConnect || needScan) {
                bluetoothPermissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.BLUETOOTH_CONNECT,
                        Manifest.permission.BLUETOOTH_SCAN
                    )
                )
            }
        }

        viewModel.initParams(userCd, gymTntCd, gymTntMei, sourceDivision, chuCode, chuName, shoCode, shoName)
        viewModel.checkBarcodeReaderConnection(context)
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
        ) {
            if (isCameraActive && hasCameraPermission) {
                Box(modifier = Modifier.size(1.dp)) {
                    OcrCameraView(
                        onTextScanned = { scannedText ->
                            viewModel.processScannedCode(scannedText)
                        }
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.Start
            ) {
                Spacer(modifier = Modifier.height(4.dp))

                // 業務担当名
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    val displayName = if (gymTntMei.isNotEmpty()) gymTntMei else viewModel.gymTntMei
                    Text(
                        text = if (displayName.isNotEmpty()) "業務担当名  $displayName" else "業務担当名",
                        fontSize = 14.sp,
                        color = Color.Black,
                        maxLines = 1
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // タイトル
                Column(
                    modifier = Modifier.width(IntrinsicSize.Max)
                ) {
                    Text(
                        text = "仕分け(鈴鹿支給品現品確定)",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        maxLines = 1
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(2.dp)
                            .background(MaterialTheme.colorScheme.primary)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // 上部アクション行
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .then(
                                if (viewModel.isEmergency) {
                                    Modifier
                                        .background(Color.Red, shape = RoundedCornerShape(8.dp))
                                        .border(1.dp, Color.Black, RoundedCornerShape(8.dp))
                                } else {
                                    Modifier
                                }
                            )
                            .padding(horizontal = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (viewModel.isEmergency) {
                            Text(
                                text = "緊急",
                                fontSize = 16.sp,
                                color = Color.Black,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = { onNavigateToMenu(userCd, gymTntCd, gymTntMei) },
                        enabled = viewModel.isMenuEnabled,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE0E0E0)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .height(48.dp)
                            .border(1.dp, Color.Black, RoundedCornerShape(8.dp))
                    ) {
                        Text(text = "仕分け・一時保管メニュー", color = Color.Black, fontSize = 13.sp, maxLines = 1)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // ボタン行
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            onNavigateToChange(
                                gymTntCd,
                                gymTntMei,
                                userCd,
                                sourceDivision
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE0E0E0)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .border(1.dp, Color.Black, RoundedCornerShape(8.dp))
                    ) {
                        Text(text = "保管BOX・保管資材変更", color = Color.Black, fontSize = 12.sp, maxLines = 1)
                    }

                    Button(
                        onClick = {
                            onNavigateToTemporary(
                                userCd,
                                gymTntCd,
                                gymTntMei,
                                sourceDivision,
                                viewModel.seihinBango
                            )
                        },
                        enabled = viewModel.isTemporaryStorageEnabled,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE0E0E0)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .border(1.dp, Color.Black, RoundedCornerShape(8.dp))
                    ) {
                        Text(text = "一時保管(保管エリア確定)", color = Color.Black, fontSize = 12.sp, maxLines = 1)
                    }
                }

                // エラーメッセージ
                if (viewModel.errorMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFFFCDD2), shape = RoundedCornerShape(4.dp))
                            .border(1.dp, Color.Red, shape = RoundedCornerShape(4.dp))
                            .padding(8.dp)
                    ) {
                        Text(text = viewModel.errorMessage!!, color = Color.Red, fontSize = 13.sp)
                    }
                }

                // インフォメッセージ
                if (viewModel.infoMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFE8F5E9), shape = RoundedCornerShape(4.dp))
                            .padding(8.dp)
                    ) {
                        Text(text = viewModel.infoMessage!!, color = Color(0xFF2E7D32), fontSize = 13.sp)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "『現品票』を読み込んでください",
                    fontSize = 12.sp,
                    color = Color.Black,
                    textAlign = TextAlign.Start
                )

                Spacer(modifier = Modifier.height(16.dp))

                // --- 各入力フィールド ---
                // 1. 保管BOX
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "保管BOX", fontSize = 14.sp, modifier = Modifier.width(90.dp), color = Color.Black)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .background(Color(0xFFD4E4F7), shape = RoundedCornerShape(4.dp))
                            .border(1.dp, Color.Black, shape = RoundedCornerShape(4.dp))
                            .padding(horizontal = 12.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        val displayBoxText = if (viewModel.shobunruiMeisho.isNotEmpty()) viewModel.shobunruiMeisho else viewModel.hokanBoxCode
                        Text(
                            text = displayBoxText,
                            fontSize = 14.sp,
                            color = if (displayBoxText.isEmpty()) Color.Gray else Color.Black,
                            maxLines = 1
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // 2. 保管資材
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.width(90.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "保管資材", fontSize = 14.sp, color = Color.Black)
                        Spacer(modifier = Modifier.width(4.dp))
                        Box(
                            modifier = Modifier
                                .size(18.dp)
                                .background(Color.Black, shape = RoundedCornerShape(9.dp))
                                .clickable { showHelpDialog = true },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "?", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .background(Color(0xFFD4E4F7), shape = RoundedCornerShape(4.dp))
                            .border(1.dp, Color.Black, shape = RoundedCornerShape(4.dp))
                            .padding(horizontal = 12.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        val displayShizaiText = if (viewModel.chubunruiMeisho.isNotEmpty()) viewModel.chubunruiMeisho else viewModel.hokanShizaiCode
                        Text(
                            text = displayShizaiText,
                            fontSize = 14.sp,
                            color = if (displayShizaiText.isEmpty()) Color.Gray else Color.Black,
                            maxLines = 1
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // 3. 製品No.
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "製品No.", fontSize = 14.sp, modifier = Modifier.width(90.dp), color = Color.Black)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .background(Color(0xFFD4E4F7), shape = RoundedCornerShape(4.dp))
                            .border(1.dp, Color.Black, shape = RoundedCornerShape(4.dp))
                            .padding(horizontal = 12.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Text(
                            text = viewModel.seihinBango,
                            fontSize = 14.sp,
                            color = if (viewModel.seihinBango.isEmpty()) Color.Gray else Color.Black,
                            maxLines = 1
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // 4. 伝票番号
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "伝票番号", fontSize = 14.sp, modifier = Modifier.width(90.dp), color = Color.Black)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .background(Color(0xFFD4E4F7), shape = RoundedCornerShape(4.dp))
                            .border(1.dp, Color.Black, shape = RoundedCornerShape(4.dp))
                            .padding(horizontal = 12.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Text(
                            text = viewModel.denpyoBango,
                            fontSize = 14.sp,
                            color = if (viewModel.denpyoBango.isEmpty()) Color.Gray else Color.Black,
                            maxLines = 1
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // 5. 支給予定数 ＆ 今回支給数
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(text = "支給予定数", fontSize = 14.sp, modifier = Modifier.width(90.dp), color = Color.Black)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .background(Color(0xFFD4E4F7), shape = RoundedCornerShape(4.dp))
                            .border(1.dp, Color.Black, shape = RoundedCornerShape(4.dp))
                            .padding(horizontal = 12.dp),
                        contentAlignment = Alignment.CenterEnd
                    ) {
                        Text(
                            text = viewModel.shikyuYoteiSu,
                            fontSize = 14.sp,
                            color = Color.Black,
                            textAlign = TextAlign.End,
                            modifier = Modifier.fillMaxWidth(),
                            maxLines = 1
                        )
                    }

                    Text(text = "今回支給数", fontSize = 14.sp, color = Color.Black)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .background(Color(0xFFFFCDD2), shape = RoundedCornerShape(4.dp))
                            .border(1.dp, Color.Black, shape = RoundedCornerShape(4.dp))
                            .padding(horizontal = 12.dp),
                        contentAlignment = Alignment.CenterEnd
                    ) {
                        Text(
                            text = viewModel.konkaiShikyuSu,
                            fontSize = 14.sp,
                            color = Color.Black,
                            textAlign = TextAlign.End,
                            modifier = Modifier.fillMaxWidth(),
                            maxLines = 1
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // 下部アクションボタン群（登録・削除）
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Button(
                        onClick = { viewModel.executeRegister() },
                        enabled = viewModel.isRegisterEnabled,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE0E0E0)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .width(130.dp)
                            .border(1.dp, Color.Black, RoundedCornerShape(8.dp))
                    ) {
                        Text(text = "登録", color = Color.Black, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = { viewModel.executeDelete() },
                        enabled = viewModel.isDeleteEnabled,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE0E0E0)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .width(130.dp)
                            .border(1.dp, Color.Black, RoundedCornerShape(8.dp))
                    ) {
                        Text(text = "削除", color = Color.Black, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = { onNavigateToEmergency(userCd, gymTntCd, gymTntMei) },
                        enabled = viewModel.isEmergencyButtonEnabled,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE0E0E0)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .width(160.dp)
                            .border(1.dp, Color.Black, RoundedCornerShape(8.dp))
                    ) {
                        Text(text = "緊急品集約", color = Color.Black, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            if (!hasCameraPermission) {
                                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                            } else {
                                isCameraActive = !isCameraActive
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE0E0E0)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .width(130.dp)
                            .border(1.dp, Color.Black, RoundedCornerShape(8.dp))
                    ) {
                        Text(
                            text = if (isCameraActive) "カメラ停止" else "カメラ起動",
                            color = Color.Black,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
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
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(text = "コンビテナ", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                                Spacer(modifier = Modifier.height(4.dp))
                                Image(
                                    painter = painterResource(id = R.drawable.conbitena),
                                    contentDescription = "コンビテナ",
                                    modifier = Modifier.size(150.dp)
                                )
                            }

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(text = "パレット", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                                Spacer(modifier = Modifier.height(4.dp))
                                Image(
                                    painter = painterResource(id = R.drawable.palette),
                                    contentDescription = "パレット",
                                    modifier = Modifier.size(150.dp)
                                )
                            }

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