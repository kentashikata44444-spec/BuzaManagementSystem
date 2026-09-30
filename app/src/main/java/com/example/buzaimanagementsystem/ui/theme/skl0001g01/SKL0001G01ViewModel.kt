package com.example.buzaimanagementsystem.ui.theme.skl0001g01

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import java.util.Calendar

class SKL0001G01ViewModel(private val apiService: SKL0001G01ApiService) : ViewModel() {

    var displayTime by mutableStateOf("00:00～00:00")
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    var isSystemAvailable by mutableStateOf(true)
        private set

    // ログイン成功したユーザーコードを保持するプロパティ
    var loggedInUserCd by mutableStateOf("")
        private set

    fun checkSystemAvailability() {
        viewModelScope.launch {
            try {
                val response = apiService.getConditions()

                if (response.operationFlag.isNullOrBlank()) {
                    isSystemAvailable = false
                    errorMessage = "稼働フラグの取得に失敗しました \nシステム管理者に問合せ願います"
                    return@launch
                }

                val isRunning = response.operationFlag.trim().equals("OK", ignoreCase = true)
                if (!isRunning) {
                    isSystemAvailable = false
                    errorMessage = "非稼働日の為、利用できません"
                    return@launch
                }

                val timeData = response.operationTime
                if (timeData == null || timeData.suji1.isNullOrBlank() || timeData.suji2.isNullOrBlank()) {
                    isSystemAvailable = false
                    errorMessage = "稼働時間の取得に失敗しました \nシステム管理者に問合せ願います"
                    return@launch
                }

                fun parseHour(value: Any?): Int? {
                    return when (value) {
                        is Number -> value.toInt()
                        is String -> value.toIntOrNull()
                        else -> value?.toString()?.toIntOrNull()
                    }
                }

                val startHour = parseHour(timeData.suji1)
                val endHour = parseHour(timeData.suji2)

                if (startHour != null && endHour != null) {
                    displayTime = "%02d:00～%02d:00".format(startHour, endHour)
                    val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)

                    val allowed = if (startHour <= endHour) {
                        currentHour in startHour until endHour
                    } else {
                        currentHour >= startHour || currentHour < endHour
                    }

                    if (!allowed) {
                        isSystemAvailable = false
                        errorMessage = "稼働時間外の為、利用できません"
                        return@launch
                    }
                } else {
                    isSystemAvailable = false
                    errorMessage = "稼働時間の取得に失敗しました \nシステム管理者に問合せ願います"
                    return@launch
                }

            } catch (e: Exception) {
                isSystemAvailable = false
                e.printStackTrace()
                errorMessage = "ＤＢ接続に失敗しました \nシステム管理者に問合せ願います"
            }
        }
    }

    // 2. ログイン処理（userCd と userName の両方をコールバックで渡すように変更）
    // 引数に userCd を追加し、計4つの値をコールバックで渡すようにする
    fun executeLogin(inputText: String, onSuccess: (userCd: String, userName: String, gymTntCd: String, gymTntMei: String) -> Unit) {
        val rawInput = inputText.trim()
        if (rawInput.isBlank()) {
            errorMessage = "ユーザーIDを入力してください。"
            return
        }

        val inputUser = rawInput.split("_", limit = 2)[0].trim()

        if (inputUser.isBlank()) {
            errorMessage = "ユーザーIDが正しくありません。"
            return
        }

        viewModelScope.launch {
            try {
                val request = SKL0001G01LoginRequest(
                    userCd = inputUser,
                    password = "",
                    userName = "",
                    bushoCd = "",
                    bushoName = ""
                )

                val response = apiService.login(request)

                if (response.success) {
                    val name = response.data?.userName?.takeIf { it.isNotBlank() } ?: inputUser
                    val tntCd = response.data?.gymTntCd ?: ""
                    val tntMei = response.data?.gymTntMei ?: ""

                    loggedInUserCd = inputUser

                    // ▼ 4つの引数をすべて呼び出し元へ返す
                    onSuccess(inputUser, name, tntCd, tntMei)
                } else {
                    errorMessage = response.message ?: "ログインに失敗しました。"
                }

            } catch (e: Exception) {
                errorMessage = "通信エラー、またはシステムエラーが発生しました。\n(${e.message})"
            }
        }
    }
}