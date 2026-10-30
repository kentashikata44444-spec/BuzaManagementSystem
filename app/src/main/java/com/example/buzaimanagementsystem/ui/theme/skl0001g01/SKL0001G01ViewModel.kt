package com.example.buzaimanagementsystem.ui.theme.skl0001g01

import android.app.Application
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.buzaimanagementsystem.utils.AppConfig
import com.example.buzaimanagementsystem.utils.BluetoothConnectionHelper
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

    var loggedInUserCd by mutableStateOf("")
        private set

    var isLoggingIn by mutableStateOf(false)
        private set

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

    fun checkSystemAvailability() {
        data class ResolvedReader(val mac: String, val name: String)
        writeLog("D", "checkSystemAvailability: システム稼働状況およびバーコードリーダーの自動紐づけを確認します")

        val context = getApplication<Application>()

        // ▼ 初回起動時や未設定時に、APIからマスタ一覧を取得してAppConfigの設定値（MAC or 銘称）と自動照合・紐づけを行う
        if (!AppConfig.hasSavedDevice(context)) {
            viewModelScope.launch {
                try {
                    val readerList = apiService.getBarcodeReaderList()
                    if (readerList.isNotEmpty()) {
                        // AppConfigに事前定義されている、あるいは端末に紐づく設定値（例: 固定の指定銘称やMAC）を取得
                        // ※ ここではConfigまたは環境等で保持している「期待するデバイス情報」と照合します
                        val configuredMac = AppConfig.getSavedBleDeviceAddress(context) // 必要に応じて個別の設定値メソッドに変更可能

                        val matchedReader = readerList.find { reader ->
                            val mac = reader.MAC_ADS?.trim()?.uppercase()
                            val name = reader.MEISHO?.trim() ?: reader.HOZON_NM?.trim()

                            // MACアドレスまたは銘称（MEISHO / HOZON_NM）でテーブルと照合
                            (!configuredMac.isNullOrBlank() && mac == configuredMac.uppercase()) ||
                                    (name == AppConfig.getConfiguredDeviceMeisho(context))
                        } ?: readerList.firstOrNull() // 一致するものがない場合はデフォルトとして先頭を割り当てる、またはエラーにする

                        if (matchedReader != null) {
                            val rawMac = matchedReader.MAC_ADS?.trim()
                            if (!rawMac.isNullOrBlank()) {
                                val formattedMac = if (!rawMac.contains(":") && rawMac.length == 12) {
                                    rawMac.chunked(2).joinToString(":")
                                } else {
                                    rawMac
                                }.uppercase()

                                AppConfig.saveBleDeviceAddress(context, formattedMac, matchedReader.HOZON_NM ?: "")
                                writeLog("I", "テーブル照合によりバーコードリーダーが自動紐づけされました: ${matchedReader.HOZON_NM} ($formattedMac)")

                                // 自動接続の開始
                                BluetoothConnectionHelper.connectToSavedDevice(
                                    context = context,
                                    onConnectionStateChanged = { isConnected, address ->
                                        writeLog("I", "自動紐づけ時接続コールバック: isConnected=$isConnected, address=$address")
                                    },
                                    onDetailedError = { _ ->
                                        writeLog("W", "自動紐づけ時BLE接続エラー（ハードウェア異常）")
                                    }
                                )
                            }
                        }
                    }
                } catch (e: Exception) {
                    writeLog("E", "バーコードリーダーマスタの自動照合に失敗しました", e)
                }
            }
        }

        viewModelScope.launch {
            try {
                val response = apiService.getConditions()
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

                if (response.operationFlag.isNullOrBlank() || !response.operationFlag.trim().equals("OK", ignoreCase = true)) {
                    isSystemAvailable = false
                    errorMessage = "非稼働日の為、利用できません"
                    return@launch
                }

                if (timeData == null || timeData.suji1.isNullOrBlank() || timeData.suji2.isNullOrBlank()) {
                    isSystemAvailable = false
                    errorMessage = "稼働時間の取得に失敗しました"
                    return@launch
                }

                val startHour = timeData.suji1.toString().toIntOrNull()
                val endHour = timeData.suji2.toString().toIntOrNull()

                if (startHour != null && endHour != null) {
                    val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
                    val allowed = if (startHour <= endHour) {
                        currentHour in startHour until endHour
                    } else {
                        currentHour >= startHour || currentHour < endHour
                    }

                    if (!allowed) {
                        isSystemAvailable = false
                        errorMessage = "稼働時間外の為、利用できません"
                    }
                }
            } catch (e: Exception) {
                isSystemAvailable = false
                writeLog("E", "checkSystemAvailability: 接続エラー", e)
                errorMessage = "ＤＢ接続に失敗しました \nシステム管理者に問合せ願います"
            }
        }
    }

    fun executeLogin(inputText: String, onSuccess: (userCd: String, userName: String, gymTntCd: String, gymTntMei: String) -> Unit) {
        if (isLoggingIn) return
        if (!isSystemAvailable) {
            if (errorMessage.isNullOrBlank()) errorMessage = "現在システムはご利用いただけません。"
            return
        }

        val inputUser = inputText.split("_", limit = 2)[0].trim()
        if (inputUser.isBlank() || inputText.any { it.isWhitespace() }) {
            errorMessage = "ユーザーが登録されていません \nユーザー登録してください"
            return
        }

        isLoggingIn = true
        errorMessage = null

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

                    if (tntCd.isBlank() || tntMei.isBlank()) {
                        errorMessage = "業務担当マスタの取得に失敗しました"
                        isLoggingIn = false
                        return@launch
                    }

                    loggedInUserCd = inputUser
                    onSuccess(inputUser, name, tntCd, tntMei)
                } else {
                    errorMessage = response.message ?: "ログインに失敗しました。"
                    isLoggingIn = false
                }
            } catch (e: Exception) {
                errorMessage = "通信エラー、またはシステムエラーが発生しました。"
                isLoggingIn = false
            }
        }
    }
}