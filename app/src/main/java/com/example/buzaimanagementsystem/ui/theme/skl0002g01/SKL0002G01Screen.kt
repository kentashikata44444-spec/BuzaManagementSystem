package com.example.buzaimanagementsystem.ui.theme.skl0002g01

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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun SKL0002G01Screen(
    viewModel: SKL0002G01ViewModel = viewModel(),
    userCd: String,
    userName: String,
    gymTntCd: String,
    gymTntMei: String,
    onNavigateToShiwake: (userCd: String, gymTntCd: String, gymTntMei: String) -> Unit,
    onNavigateToShuyaku: () -> Unit,
    onNavigateToHikiwatashi: () -> Unit,
    onNavigateToHenkyaku: () -> Unit,
    onNavigateToKobetsu: () -> Unit,
    onExitApp: () -> Unit
) {
    LaunchedEffect(userCd, userName, gymTntCd, gymTntMei) {
        viewModel.initUserData(userCd, userName, gymTntCd, gymTntMei)
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

            // 1段目：業務担当名（右寄せ）
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                val displayName = if (gymTntMei.isNotEmpty()) gymTntMei else viewModel.gymTntMei
                Text(
                    text = if (displayName.isNotEmpty()) "業務担当名: $displayName" else "業務担当名",
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
                        text = "メインメニュー",
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

            // 3段目：終了ボタン（右寄せ・【ここだけ】グレー背景＆黒枠に指定）
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Button(
                    onClick = { viewModel.onExitClicked(onExitApp) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE0E0E0)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .width(120.dp)
                        .border(1.dp, Color.Black, RoundedCornerShape(8.dp))
                ) {
                    Text(text = "終了", fontSize = 14.sp, color = Color.Black, maxLines = 1)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // 中央：各業務メニューボタン群（こちらは通常の標準ボタンデザインのまま）
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
                    MenuButton(text = "仕分け・一時保管") {
                        viewModel.onShiwakeClicked {
                            onNavigateToShiwake(
                                viewModel.userCd,
                                viewModel.gymTntCd,
                                viewModel.gymTntMei
                            )
                        }
                    }
                    MenuButton(text = "集約") {
                        viewModel.onShuyakuClicked(onNavigateToShuyaku)
                    }
                    MenuButton(text = "引渡し") {
                        viewModel.onHikiwatashiClicked(onNavigateToHikiwatashi)
                    }
                    MenuButton(text = "返却品処理") {
                        viewModel.onHenkyakuClicked(onNavigateToHenkyaku)
                    }
                    MenuButton(text = "その他個別作業") {
                        viewModel.onKobetsuClicked(onNavigateToKobetsu)
                    }
                }
            }
        }
    }
}

// 中央の業務メニューボタン（デフォルトデザインのまま）
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