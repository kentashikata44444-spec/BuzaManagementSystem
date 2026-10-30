package com.example.buzaimanagementsystem.ui.theme.skl0100g01

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

private const val TAG = "SKL0100G01Screen"

@Composable
fun SKL0100G01Screen(
    viewModel: SKL0100G01ViewModel = viewModel(),
    userCd: String,
    gymTntCd: String,
    gymTntMei: String,
    onNavigateToMain: (userCd: String, gymTntCd: String, gymTntMei: String) -> Unit,
    onNavigateToKobeTsukuba: (userCd: String, gymTntCd: String, gymTntMei: String, sourceDivision: String) -> Unit,
    onNavigateToSuzuka: (userCd: String, gymTntCd: String, gymTntMei: String, sourceDivision: String) -> Unit,
    onNavigateToKatsueiWarehouse: (userCd: String, gymTntCd: String, gymTntMei: String, sourceDivision: String) -> Unit
) {
    val context = LocalContext.current

    // 画面表示時にViewModelへパラメータを保持させる ＆ 初期化引数をログ出力
    LaunchedEffect(userCd, gymTntCd, gymTntMei) {
        FileLogger.writeLog(context, "DEBUG", TAG, "画面初期化パラメータ受信: userCd=$userCd, gymTntCd=$gymTntCd, gymTntMei=$gymTntMei")
        viewModel.initParams(userCd, gymTntCd, gymTntMei)
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
            // 上部ヘッダー（3段構成）
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

            // 2段目：タイトルと下線
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start
            ) {
                Column(
                    modifier = Modifier.width(IntrinsicSize.Max)
                ) {
                    Text(
                        text = "仕分け・一時保管メニュー",
                        fontSize = 22.sp,
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

            // 3段目：メインメニューボタン（右寄せ）
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Button(
                    onClick = {
                        FileLogger.writeLog(context, "DEBUG", TAG, "メインメニューボタン押下: userCd=${viewModel.userCd}, gymTntCd=${viewModel.gymTntCd}, gymTntMei=${viewModel.gymTntMei}")
                        viewModel.onButtonClicked {
                            onNavigateToMain(viewModel.userCd, viewModel.gymTntCd, viewModel.gymTntMei)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE0E0E0)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .width(150.dp)
                        .border(1.dp, Color.Black, RoundedCornerShape(8.dp))
                ) {
                    Text(text = "メインメニュー", fontSize = 14.sp, color = Color.Black, maxLines = 1)
                }
            }

            // ==========================================

            Spacer(modifier = Modifier.height(24.dp))

            // 中央：各仕分けメニューボタン群
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    MenuButton(text = "神戸支給品") {
                        val sourceDivision = "1" //
                        FileLogger.writeLog(context, "DEBUG", TAG, "神戸・筑波支給品ボタン押下: userCd=${viewModel.userCd}, gymTntCd=${viewModel.gymTntCd}, gymTntMei=${viewModel.gymTntMei}, sourceDivision=$sourceDivision")
                        viewModel.onButtonClicked {
                            onNavigateToKobeTsukuba(viewModel.userCd, viewModel.gymTntCd, viewModel.gymTntMei, sourceDivision)
                        }
                    }
                    MenuButton(text = "鈴鹿支給品") {
                        val sourceDivision = "2" //[cite: 2]
                        FileLogger.writeLog(context, "DEBUG", TAG, "鈴鹿支給品ボタン押下: userCd=${viewModel.userCd}, gymTntCd=${viewModel.gymTntCd}, gymTntMei=${viewModel.gymTntMei}, sourceDivision=$sourceDivision")
                        viewModel.onButtonClicked {
                            onNavigateToSuzuka(viewModel.userCd, viewModel.gymTntCd, viewModel.gymTntMei, sourceDivision)
                        }
                    }
                    MenuButton(text = "勝英購入品・倉庫品") {
                        val sourceDivision = "3" //[cite: 2]
                        FileLogger.writeLog(context, "DEBUG", TAG, "勝英購入品・倉庫品ボタン押下: userCd=${viewModel.userCd}, gymTntCd=${viewModel.gymTntCd}, gymTntMei=${viewModel.gymTntMei}, sourceDivision=$sourceDivision")
                        viewModel.onButtonClicked {
                            onNavigateToKatsueiWarehouse(viewModel.userCd, viewModel.gymTntCd, viewModel.gymTntMei, sourceDivision)
                        }
                    }
                }
            }
        }
    }
}

// 中央の仕分けメニューボタン（デフォルトデザイン）
@Composable
private fun MenuButton(text: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .width(240.dp)
            .height(50.dp)
    ) {
        Text(text = text, fontSize = 16.sp)
    }
}