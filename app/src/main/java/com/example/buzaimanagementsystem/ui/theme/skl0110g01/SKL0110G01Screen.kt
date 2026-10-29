package com.example.buzaimanagementsystem.ui.theme.skl0110g01

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.buzaimanagementsystem.R
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
    var showHelpDialog by remember { mutableStateOf(false) }

    LaunchedEffect(userCd, gymTntCd, gymTntMei, sourceDivision, seihinBango) {
        FileLogger.writeLog(context, "DEBUG", TAG, "画面初期化パラメータ受信: seihinBango=$seihinBango")
        viewModel.initParams(userCd, gymTntCd, gymTntMei, sourceDivision, seihinBango)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // ★ 追加: APIのデータ取得（判定）が完了するまで画面を描画せず白紙でガードし、一瞬のチラつきを防ぐ
        if (viewModel.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White)
            )
        } else {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = Color.Transparent
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

                    // タイトル
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

                    // アクションボタン行
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Button(
                            onClick = { onNavigateToChange() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE0E0E0)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .width(150.dp)
                                .border(1.dp, Color.Black, RoundedCornerShape(8.dp))
                        ) {
                            Text(text = "保管資材変更", fontSize = 13.sp, color = Color.Black, maxLines = 1)
                        }

                        Button(
                            onClick = { onNavigateToTemporary() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE0E0E0)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .width(170.dp)
                                .border(1.dp, Color.Black, RoundedCornerShape(8.dp))
                        ) {
                            Text(
                                text = "一時保管(保管エリア確定)",
                                fontSize = 10.sp,
                                color = Color.Black,
                                maxLines = 1
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // ==========================================
                    // 中央メイン入力表示エリア
                    // ==========================================
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // 1. 製品No.
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
                                Text(text = seihinBango, fontSize = 16.sp, color = Color.Black)
                            }
                        }

                        // ==========================================
                        // ★ 保管資材メッセージエリア（製品No.と保管資材の間）
                        // ==========================================
                        if (viewModel.shizaiMessage.isNotEmpty()) {
                            val isError = viewModel.shizaiMessage.contains("設定されていません") ||
                                    viewModel.shizaiMessage.contains("存在しません") ||
                                    viewModel.shizaiMessage.contains("エラー") ||
                                    viewModel.shizaiMessage.contains("以外が読み込まれました") ||
                                    viewModel.shizaiMessage.contains("対象外です") ||
                                    viewModel.shizaiMessage.contains("システム管理者に連絡してください")

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(if (isError) Color(0xFFFFCDD2) else Color.Transparent, shape = RoundedCornerShape(4.dp))
                                    .border(1.dp, if (isError) Color.Red else Color.Gray, shape = RoundedCornerShape(4.dp))
                                    .padding(8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = viewModel.shizaiMessage,
                                    fontSize = 12.sp,
                                    color = if (isError) Color.Red else Color.Black
                                )
                            }
                        }

                        // 2. 保管資材（「保管資材」ラベル ＋ 黒丸「？」アイコン / タップでモーダル表示）
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier
                                    .width(90.dp)
                                    .clickable { showHelpDialog = true },
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = "保管資材", fontSize = 14.sp)

                                // 黒丸の「？」ヘルプアイコン
                                Box(
                                    modifier = Modifier
                                        .size(20.dp)
                                        .background(Color.Black, shape = CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "?",
                                        fontSize = 12.sp,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(4.dp))

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

                        // ==========================================
                        // ★ 保管BOXメッセージエリア（保管資材と保管BOXの間）
                        // ==========================================
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
                        } else if (viewModel.boxMessage.isNotEmpty()) {
                            val isBoxError = viewModel.boxMessage.contains("存在しません") ||
                                    viewModel.boxMessage.contains("エラー") ||
                                    viewModel.boxMessage.contains("システム管理者に連絡してください")

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(if (isBoxError) Color(0xFFFFCDD2) else Color.Transparent, shape = RoundedCornerShape(4.dp))
                                    .border(1.dp, if (isBoxError) Color.Red else Color.Gray, shape = RoundedCornerShape(4.dp))
                                    .padding(8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = viewModel.boxMessage,
                                    fontSize = 12.sp,
                                    color = if (isBoxError) Color.Red else Color.Black
                                )
                            }
                        }

                        // 3. 保管BOX
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

                    Spacer(modifier = Modifier.height(20.dp))

                    // ==========================================
                    // 下部一覧表示エリア
                    // ==========================================
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (viewModel.hokanBoxList.isNotEmpty()) {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(viewModel.hokanBoxList) { boxItem ->
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(48.dp)
                                            .background(Color(0xFFD0E1F9), RoundedCornerShape(4.dp))
                                            .border(1.dp, Color.Gray, RoundedCornerShape(4.dp))
                                            .padding(horizontal = 12.dp),
                                        contentAlignment = Alignment.CenterStart
                                    ) {
                                        Text(
                                            text = "${boxItem.hokanBoxCd} : ${boxItem.hokanBoxMeisho}",
                                            fontSize = 16.sp,
                                            color = Color.Black
                                        )
                                    }
                                }
                            }
                        } else {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {}
                        }
                    }
                }
            }
        }

        // === 保管資材説明モーダル（AlertDialog）を最前面に配置 ===
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

                        // 台車
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