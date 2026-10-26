package com.example.buzaimanagementsystem.ui.theme.skl0110g01

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
import com.example.buzaimanagementsystem.utils.FileLogger

private const val TAG = "SKL0110G01Screen"

@Composable
fun SKL0110G01Screen(
    viewModel: SKL0110G01ViewModel,
    userCd: String,
    gymTntCd: String,
    gymTntMei: String,
    sourceDivision: String,
    seihinBango: String,
    onNavigateToMenu: () -> Unit,
    onNavigateToChange: () -> Unit,
    onNavigateToTemporary: () -> Unit
) {
    val context = LocalContext.current

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
            Spacer(modifier = Modifier.height(8.dp))

            // ==========================================
            // 上部ヘッダーエリア
            // ==========================================

            // 1段目：業務担当名（右寄せ）
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                val displayName = if (gymTntMei.isNotEmpty()) gymTntMei else viewModel.gymTntMei
                Text(
                    text = if (displayName.isNotEmpty()) "業務担当名  $displayName" else "業務担当名",
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 2段目：タイトルと下線（左寄せ）- 「仕分け(保管資材確定)」
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start
            ) {
                Column(
                    modifier = Modifier.width(IntrinsicSize.Max)
                ) {
                    Text(
                        text = "仕分け(保管資材確定)",
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
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 3段目：上部アクションボタン行（保管資材変更 / 一時保管(保管エリア確)）
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(
                    onClick = {
                        FileLogger.writeLog(context, "DEBUG", TAG, "保管資材変更ボタン押下")
                        onNavigateToChange()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE0E0E0)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .width(150.dp)
                        .border(1.dp, Color.Black, RoundedCornerShape(8.dp))
                ) {
                    Text(text = "保管資材変更", fontSize = 13.sp, color = Color.Black, maxLines = 1)
                }

                Button(
                    onClick = {
                        FileLogger.writeLog(context, "DEBUG", TAG, "一時保管(保管エリア確)ボタン押下")
                        onNavigateToTemporary()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE0E0E0)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .width(170.dp)
                        .border(1.dp, Color.Black, RoundedCornerShape(8.dp))
                ) {
                    Text(
                        text = "一時保管(保管エリア確)",
                        fontSize = 12.sp,
                        color = Color.Black,
                        maxLines = 1
                    )
                }
            }

            // 一般的な情報メッセージ表示
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

            // ==========================================
            // 中央メイン表示エリア
            // ==========================================
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 1. 製品No. 行
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "製品No.", fontSize = 14.sp, modifier = Modifier.width(90.dp))
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

                // ★ 保管資材メッセージエリア（製品No. と 保管資材の間）
                if (viewModel.errorMessage != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFFFCDD2), shape = RoundedCornerShape(4.dp))
                            .border(1.dp, Color.Red, shape = RoundedCornerShape(4.dp))
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = viewModel.errorMessage!!, color = Color.Red, fontSize = 13.sp)
                    }
                } else if (viewModel.shizaiMessage.isNotEmpty()) {
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

                // 2. 保管資材 行（ヘルプアイコン付きラベル等にも対応可能）
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "保管資材", fontSize = 14.sp, modifier = Modifier.width(90.dp))
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

                // ★ 保管BOXメッセージエリア（保管資材 と 保管BOXの間）
                if (viewModel.boxMessage.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Color.Gray, RoundedCornerShape(4.dp))
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = viewModel.boxMessage, fontSize = 12.sp, color = Color.DarkGray)
                    }
                }

                // 3. 保管BOX 行
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "保管BOX", fontSize = 14.sp, modifier = Modifier.width(90.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .background(Color(0xFFD0E1F9), RoundedCornerShape(4.dp))
                            .border(1.dp, Color.Gray, RoundedCornerShape(4.dp))
                            .padding(horizontal = 12.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Text(text = viewModel.hokanBoxName, fontSize = 16.sp, color = Color.Black)
                    }
                }
            }
        }
    }
}