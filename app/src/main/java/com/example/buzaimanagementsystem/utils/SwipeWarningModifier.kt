package com.example.buzaimanagementsystem.utils

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight

@Composable
fun SwipeWarningBox(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    var showWarningDialog by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            // 上方向へのスワイプ検知のみ
            .pointerInput(Unit) {
                var dragSumY = 0f
                detectDragGestures(
                    onDragStart = {
                        dragSumY = 0f
                    },
                    onDrag = { _, dragAmount ->
                        dragSumY += dragAmount.y
                    },
                    onDragEnd = {
                        // 上方向へ一定以上（-100ピクセル以上）スワイプされた場合
                        if (dragSumY < -100f) {
                            showWarningDialog = true
                        }
                    },
                    onDragCancel = {
                        dragSumY = 0f
                    }
                )
            }
    ) {
        // 本来の画面コンテンツ
        content()

        // 注意喚起ダイアログ
        if (showWarningDialog) {
            AlertDialog(
                onDismissRequest = { showWarningDialog = false },
                title = {
                    Text(
                        text = "⚠️ 操作に関するご注意",
                        color = Color.Red,
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Text(
                        text = "スワイプ操作は、禁止です。\n\n" +
                                "アプリを終了したい場合は、必ずメニューの「終了」ボタンを押してください。"
                    )
                },
                confirmButton = {
                    Button(onClick = { showWarningDialog = false }) {
                        Text("確認")
                    }
                }
            )
        }
    }
}