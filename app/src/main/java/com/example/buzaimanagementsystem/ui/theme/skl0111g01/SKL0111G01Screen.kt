package com.example.buzaimanagementsystem.ui.theme.skl0111g01

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.buzaimanagementsystem.utils.FileLogger

private const val TAG = "SKL0111G01Screen"

@Composable
fun SKL0111G01Screen(
    viewModel: SKL0111G01ViewModel = viewModel(),
    userCd: String,
    gymTntCd: String,
    gymTntMei: String,
    sourceDivision: String,
    seihinBango: String,
    onNavigateToMenu: () -> Unit,
    onNavigateToAreaChange: () -> Unit,
    onNavigateToHokanAreaKakutei: () -> Unit
) {
    val context = LocalContext.current

    // 画面初期化時にパラメータをViewModelへ伝達
    LaunchedEffect(userCd, gymTntCd, gymTntMei, sourceDivision, seihinBango) {
        FileLogger.writeLog(context, "DEBUG", TAG, "画面初期化パラメータ受信: userCd=$userCd, gymTntCd=$gymTntCd, sourceDivision=$sourceDivision, seihinBango=$seihinBango")
        viewModel.initParams(userCd, gymTntCd, gymTntMei, sourceDivision, seihinBango)
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ==========================================
            // 上部ヘッダー
            // ==========================================
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // タイトルと下線
                Column(
                    modifier = Modifier.width(IntrinsicSize.Max)
                ) {
                    Text(
                        text = "一時保管(保管エリア確定)",
                        fontSize = 20.sp,
                        style = MaterialTheme.typography.titleLarge,
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

                // 業務担当名（右寄せ）
                Text(
                    text = if (gymTntMei.isNotEmpty()) "業務担当名  $gymTntMei" else "業務担当名",
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ==========================================
            // 上部アクションボタン行（保管エリア変更 / 仕分け・一時保管メニュー）
            // ==========================================
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // 保管エリア変更ボタン（常に有効）
                Button(
                    onClick = {
                        FileLogger.writeLog(context, "DEBUG", TAG, "保管エリア変更ボタン押下")
                        onNavigateToAreaChange()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE0E0E0)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .width(150.dp)
                        .border(1.dp, Color.Black, RoundedCornerShape(8.dp))
                ) {
                    Text(text = "保管エリア変更", fontSize = 13.sp, color = Color.Black, maxLines = 1)
                }

                // 仕分け・一時保管メニューボタン（仕分け登録が0件の場合は有効、1件以上の場合は無効）
                val isMenuEnabled = viewModel.registeredCount == 0
                Button(
                    onClick = {
                        if (isMenuEnabled) {
                            FileLogger.writeLog(context, "DEBUG", TAG, "仕分け・一時保管メニューボタン押下")
                            onNavigateToMenu()
                        }
                    },
                    enabled = isMenuEnabled,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isMenuEnabled) Color(0xFFE0E0E0) else Color.LightGray
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .width(170.dp)
                        .border(1.dp, if (isMenuEnabled) Color.Black else Color.Gray, RoundedCornerShape(8.dp))
                ) {
                    Text(
                        text = "仕分け・一時保管メニュー",
                        fontSize = 12.sp,
                        color = if (isMenuEnabled) Color.Black else Color.DarkGray,
                        maxLines = 1
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // ==========================================
            // 中央メイン表示エリア（製品No / 保管エリア / 保管資材）
            // ==========================================
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 製品No.
                Column {
                    Text(text = "製品No.", fontSize = 14.sp, modifier = Modifier.padding(bottom = 4.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .background(Color(0xFFD0E1F9), RoundedCornerShape(4.dp))
                            .border(1.dp, Color.Gray, RoundedCornerShape(4.dp))
                            .padding(horizontal = 12.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Text(text = viewModel.seihinBango, fontSize = 16.sp, color = Color.Black)
                    }
                }

                // 保管エリアメッセージ
                if (viewModel.areaMessage.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Color.Gray, RoundedCornerShape(4.dp))
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = viewModel.areaMessage, fontSize = 12.sp, color = Color.DarkGray)
                    }
                }

                // 保管エリア
                Column {
                    Text(text = "保管エリア", fontSize = 14.sp, modifier = Modifier.padding(bottom = 4.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .background(Color(0xFFD0E1F9), RoundedCornerShape(4.dp))
                            .border(1.dp, Color.Gray, RoundedCornerShape(4.dp))
                            .padding(horizontal = 12.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Text(text = viewModel.hokanAreaName, fontSize = 16.sp, color = Color.Black)
                    }
                }

                // 保管資材メッセージ
                if (viewModel.shizaiMessage.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Color.Gray, RoundedCornerShape(4.dp))
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = viewModel.shizaiMessage, fontSize = 12.sp, color = Color.DarkGray)
                    }
                }

                // 保管資材
                Column {
                    Text(text = "保管資材", fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .background(Color(0xFFD0E1F9), RoundedCornerShape(4.dp))
                            .border(1.dp, Color.Gray, RoundedCornerShape(4.dp))
                            .padding(horizontal = 12.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Text(text = viewModel.hokanShizaiName, fontSize = 16.sp, color = Color.Black)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ==========================================
            // 最下部ボタン（一時保管・保管エリア確定）
            // ==========================================
            val isKakuteiEnabled = viewModel.registeredCount > 0
            Button(
                onClick = {
                    if (isKakuteiEnabled) {
                        FileLogger.writeLog(context, "DEBUG", TAG, "一時保管(保管エリア確定)ボタン押下")
                        onNavigateToHokanAreaKakutei()
                    }
                },
                enabled = isKakuteiEnabled,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(55.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isKakuteiEnabled) MaterialTheme.colorScheme.primary else Color.LightGray
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "一時保管(保管エリア確定)",
                    fontSize = 16.sp,
                    color = if (isKakuteiEnabled) Color.White else Color.DarkGray
                )
            }
        }
    }
}