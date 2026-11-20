package com.example.buzaimanagementsystem.utils

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale

@Composable
fun NumericKeypadDialog(
    initialValue: String,
    title: String = "今回支給数入力",
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    // テンキー表示時は常にクリアした状態から開始
    var inputText by remember {
        mutableStateOf("")
    }

    fun canAppendDecimalDigit(): Boolean {
        val dotIndex = inputText.indexOf('.')
        if (dotIndex == -1) return true
        return inputText.substring(dotIndex + 1).length < 2
    }

    fun appendChar(ch: String) {
        if (ch == ".") {
            if (inputText.contains(".")) return
            inputText = if (inputText.isEmpty()) "0." else "$inputText."
            return
        }
        if (!canAppendDecimalDigit()) return
        inputText = if (inputText == "0") ch else inputText + ch
    }

    fun normalizeForConfirm(raw: String): String {
        if (raw.isEmpty()) return "0"
        val trimmedDot = raw.trimEnd('.')
        if (trimmedDot.isEmpty()) return "0"
        val parsed = trimmedDot.toDoubleOrNull() ?: return "0"
        return if (parsed % 1.0 == 0.0) {
            parsed.toInt().toString()
        } else {
            String.format(Locale.US, "%.2f", parsed).trimEnd('0').trimEnd('.')
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        shape = RoundedCornerShape(8.dp),
        title = {
            Text(
                text = title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .background(Color(0xFFFFCDD2), shape = RoundedCornerShape(4.dp))
                        .border(1.dp, Color.Black, shape = RoundedCornerShape(4.dp))
                        .padding(horizontal = 12.dp),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    Text(
                        text = inputText.ifEmpty { "0" },
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        maxLines = 1
                    )
                }

                val rows = listOf(
                    listOf("7", "8", "9"),
                    listOf("4", "5", "6"),
                    listOf("1", "2", "3"),
                    listOf(".", "0", "C")
                )

                rows.forEach { rowKeys ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        rowKeys.forEach { key ->
                            Button(
                                onClick = {
                                    if (key == "C") {
                                        inputText = ""
                                    } else {
                                        appendChar(key)
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (key == "C") Color(0xFFFFCDD2) else Color(0xFFE0E0E0),
                                    contentColor = Color.Black
                                ),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(52.dp)
                                    .border(1.dp, Color.Black, RoundedCornerShape(8.dp))
                            ) {
                                Text(
                                    text = key,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(normalizeForConfirm(inputText)) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFE0E0E0),
                    contentColor = Color.Black
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.border(1.dp, Color.Black, RoundedCornerShape(8.dp))
            ) {
                Text(text = "確定", fontSize = 15.sp, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            Button(
                onClick = {
                    // 入力せずに閉じる場合は元の値をそのまま維持（onConfirmは呼ばない）
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFF5F5F5),
                    contentColor = Color.Black
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.border(1.dp, Color.Gray, RoundedCornerShape(8.dp))
            ) {
                Text(text = "閉じる", fontSize = 15.sp)
            }
        }
    )
}