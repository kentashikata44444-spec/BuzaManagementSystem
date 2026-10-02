package com.example.buzaimanagementsystem.ui.theme.skl0001g01

import android.app.Application
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.buzaimanagementsystem.utils.FileLogger // 先ほどのFileLoggerクラス
import kotlinx.coroutines.launch
import java.util.Calendar

class SKL0001G01ViewModel(
    application: Application,
    private val apiService: SKL0001G01ApiService
) : AndroidViewModel(application) {

    companion object {
        private const val TAG = "SKL0001G01ViewModel"
    }

    var displayTime by mutableStateOf("00:00～00:00")
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    var isSystemAvailable by mutableStateOf(true)
        private set

    // ログイン成功したユーザーコードを保持するプロパティ
    var loggedInUserCd by mutableStateOf("")
        private set

    /**
     * 内部共通ログ出力用（Logcat ＆ テキストファイルへ同時出力）
     */
    private fun writeLog(level: String, message: String, throwable: Throwable? = null) {
        val context = getApplication<Application>()
        when (level) {
            "D" -> {
                Log.d(TAG, message, throwable)
                FileLogger.writeLog(context, "DEBUG", TAG, message + (throwable?.let { " : ${it.message}" } ?: ""))
            }
            "W" -> {
                Log.w(TAG, message, throwable)
                FileLogger.writeLog(context, "WARN", TAG, message + (throwable?.let { " : ${it.message}" } ?: ""))
            }
            "E" -> {
                Log.e(TAG, message, throwable)
                FileLogger.writeLog(context, "ERROR", TAG, message + (throwable?.let { " : ${it.message}" } ?: ""))
            }
            "I" -> {
                Log.i(TAG, message, throwable)
                FileLogger.writeLog(context, "INFO", TAG, message + (throwable?.let { " : ${it.message}" } ?: ""))
            }
        }
    }

    /**
     * システムの稼働状況および稼働時間をチェックする
     */
    fun checkSystemAvailability() {
        writeLog("D", "checkSystemAvailability: システム稼働状況の確認を開始します")
        viewModelScope.launch {
            try {
                val response = apiService.getConditions()

                if (response.operationFlag.isNullOrBlank()) {
                    writeLog("W", "checkSystemAvailability: 稼働フラグが取得できませんでした")
                    isSystemAvailable = false
                    errorMessage = "稼働フラグの取得に失敗しました \nシステム管理者に問合せ願います"
                    return@launch
                }

                val isRunning = response.operationFlag.trim().equals("Y", ignoreCase = true)
                if (!isRunning) {
                    writeLog("W", "checkSystemAvailability: 非稼働日のため利用不可 (operationFlag=${response.operationFlag})")
                    isSystemAvailable = false
                    errorMessage = "非稼働日の為、利用できません"
                    return@launch
                }

                val timeData = response.operationTime
                if (timeData == null || timeData.suji1.isNullOrBlank() || timeData.suji2.isNullOrBlank()) {
                    writeLog("W", "checkSystemAvailability: 稼働時間のデータが不正です")
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

                    writeLog("D", "checkSystemAvailability: 設定時間=$displayTime, 現在の時刻(時)=$currentHour, 判定=$allowed")

                    if (!allowed) {
                        isSystemAvailable = false
                        errorMessage = "稼働時間外の為、利用できません"
                        return@launch
                    }
                } else {
                    writeLog("W", "checkSystemAvailability: 開始・終了時間のパースに失敗しました")
                    isSystemAvailable = false
                    errorMessage = "稼働時間の取得に失敗しました \nシステム管理者に問合せ願います"
                    return@launch
                }

                writeLog("D", "checkSystemAvailability: システム稼働チェック完了（利用可能）")

            } catch (e: Exception) {
                isSystemAvailable = false
                writeLog("E", "checkSystemAvailability: DB接続エラーが発生しました", e)
                errorMessage = "ＤＢ接続に失敗しました \nシステム管理者に問合せ願います"
            }
        }
    }

    /**
     * ログイン処理を実行する
     */
    fun executeLogin(inputText: String, onSuccess: (userCd: String, userName: String, gymTntCd: String, gymTntMei: String) -> Unit) {
        writeLog("D", "executeLogin: ログイン処理を開始します (入力長: ${inputText.length})")

        // システム利用不可（非稼働日・時間外）の場合は処理をブロック
        if (!isSystemAvailable) {
            writeLog("W", "executeLogin: システム利用不可のためログインをブロックしました")
            if (errorMessage.isNullOrBlank()) {
                errorMessage = "現在システムはご利用いただけません。"
            }
            return
        }

        val rawInput = inputText.trim()
        if (rawInput.isBlank()) {
            writeLog("W", "executeLogin: 入力文字列が空です")
            errorMessage = "ユーザーIDを入力してください。"
            return
        }

        // "_" 区切りのプレフィックスなどを考慮した処理
        val inputUser = rawInput.split("_", limit = 2)[0].trim()

        if (inputUser.isBlank()) {
            writeLog("W", "executeLogin: パース後のユーザーIDが空です")
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

                writeLog("D", "executeLogin: APIへログインリクエスト送信 (userCd=$inputUser)")
                val response = apiService.login(request)

                if (response.success) {
                    val name = response.data?.userName?.takeIf { it.isNotBlank() } ?: inputUser
                    val tntCd = response.data?.gymTntCd ?: ""
                    val tntMei = response.data?.gymTntMei ?: ""

                    loggedInUserCd = inputUser
                    writeLog("I", "executeLogin: ログイン成功 (userCd=$inputUser, name=$name, 業務担当=($tntCd:$tntMei))")

                    onSuccess(inputUser, name, tntCd, tntMei)
                } else {
                    writeLog("W", "executeLogin: ログイン失敗メッセージ: ${response.message}")
                    errorMessage = response.message ?: "ログインに失敗しました。"
                }

            } catch (e: Exception) {
                writeLog("E", "executeLogin: 通信・システムエラーが発生しました", e)
                errorMessage = "通信エラー、またはシステムエラーが発生しました。\n(${e.message})"
            }
        }
    }
}