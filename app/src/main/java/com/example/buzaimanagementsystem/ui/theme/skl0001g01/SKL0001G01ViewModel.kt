package com.example.buzaimanagementsystem.ui.theme.skl0001g01

import android.app.Application
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.buzaimanagementsystem.utils.FileLogger
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

    // ログイン処理中の二重実行・ループを防ぐためのフラグ
    var isLoggingIn by mutableStateOf(false)
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

                // まずは稼働時間を先に取得して表示用文字列（displayTime）を確定させる
                val timeData = response.operationTime
                if (timeData != null && !timeData.suji1.isNullOrBlank() && !timeData.suji2.isNullOrBlank()) {
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
                    }
                }

                // 次に稼働フラグのチェックを行う
                if (response.operationFlag.isNullOrBlank()) {
                    writeLog("W", "checkSystemAvailability: 稼働フラグが取得できませんでした")
                    isSystemAvailable = false
                    errorMessage = "稼働フラグの取得に失敗しました \nシステム管理者に問合せ願います"
                    return@launch
                }

                val isRunning = response.operationFlag.trim().equals("OK", ignoreCase = true)
                if (!isRunning) {
                    writeLog("W", "checkSystemAvailability: 非稼働日のため利用不可 (operationFlag=${response.operationFlag})")
                    isSystemAvailable = false
                    errorMessage = "非稼働日の為、利用できません"
                    return@launch
                }

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
                writeLog("E", "checkSystemAvailability: 稼働状況チェック時のDB接続エラーが発生しました", e)
                errorMessage = "ＤＢ接続に失敗しました \nシステム管理者に問合せ願います"
            }
        }
    }

    /**
     * ログイン処理を実行する
     */
    fun executeLogin(inputText: String, onSuccess: (userCd: String, userName: String, gymTntCd: String, gymTntMei: String) -> Unit) {
        // すでにログイン処理中の場合は重複リクエストをブロック
        if (isLoggingIn) {
            writeLog("W", "executeLogin: 既にログイン処理中のため、リクエストをスキップします")
            return
        }

        writeLog("D", "executeLogin: ログイン処理を開始します (入力長: ${inputText.length})")

        // システム利用不可（非稼働日・時間外）の場合は処理をブロック
        if (!isSystemAvailable) {
            writeLog("W", "executeLogin: システム利用不可のためログインをブロックしました")
            if (errorMessage.isNullOrBlank()) {
                errorMessage = "現在システムはご利用いただけません。"
            }
            return
        }

        val rawInput = inputText
        if (rawInput.isBlank()) {
            writeLog("W", "executeLogin: 入力文字列が空です")
            errorMessage = "ユーザーIDを入力してください。"
            return
        }

        // ▼ 全角・半角スペース（空白文字）が含まれている場合は拒否する
        if (rawInput.any { it.isWhitespace() }) {
            writeLog("W", "executeLogin: 入力値にスペースが含まれているため拒否します")
            errorMessage = "このQRコードは使用できません。"
            return
        }

        val inputUser = rawInput.split("_", limit = 2)[0].trim()

        if (inputUser.isBlank()) {
            writeLog("W", "executeLogin: パース後のユーザーIDが空です")
            errorMessage = "ユーザーIDが正しくありません。"
            return
        }

        // 処理開始：フラグをONにする
        isLoggingIn = true
        errorMessage = null // ログイン試行時は一度エラーメッセージをクリア

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

                    // 業務担当マスタの取得チェック
                    if (tntCd.isBlank() || tntMei.isBlank()) {
                        writeLog("W", "executeLogin: 業務担当マスタの取得に失敗しました（データ不備） (userCd=$inputUser)")
                        errorMessage = "業務担当マスタの取得に失敗しました \nシステム管理者に問合せ願います"
                        isLoggingIn = false
                        return@launch
                    }

                    loggedInUserCd = inputUser
                    writeLog("I", "executeLogin: ログイン成功 (userCd=$inputUser, name=$name, 業務担当=($tntCd:$tntMei))")

                    onSuccess(inputUser, name, tntCd, tntMei)
                } else {
                    // サーバー側で「すでにログイン中」などのビジネスロジックエラー
                    writeLog("W", "executeLogin: ログイン失敗（サーバー判定）: ${response.message} (userCd=$inputUser)")
                    errorMessage = response.message ?: "ログインに失敗しました。"
                    isLoggingIn = false
                }

            } catch (e: Exception) {
                // 通信エラーや予期せぬシステム例外
                writeLog("E", "executeLogin: ログイン処理中の通信・システムエラーが発生しました (userCd=$inputUser)", e)
                errorMessage = "通信エラー、またはシステムエラーが発生しました。\n(${e.message})"
                isLoggingIn = false
            }
        }
    }
}