package com.example.buzaimanagementsystem.utils

import android.content.Context
import android.util.Log
import java.io.File

object AppConfig {
    private const val TAG = "AppConfig"

    private const val PREF_NAME = "BuzaManagementAppConfig"
    private const val KEY_SAVED_BLE_ADDRESS = "saved_ble_address"
    private const val KEY_SAVED_HOZON_NM = "saved_hozon_nm"

    /**
     * server_config.txt の "BLE_MAC=" からバーコードリーダーのMACアドレスを取得する。
     * ファイルに記載がない場合は SharedPreferences にフォールバックする。
     */
    fun getSavedBleDeviceAddress(context: Context): String? {
        val configFile = getConfigFile(context)
        if (configFile != null && configFile.exists()) {
            try {
                val content = configFile.readText()
                for (rawLine in content.lines()) {
                    val line = rawLine.trim()
                    if (line.isEmpty() || line.startsWith("#") || line.startsWith(";")) continue

                    if (line.uppercase().startsWith("BLE_MAC=")) {
                        val rawMac = line.substringAfter("=").trim()
                        if (rawMac.isNotEmpty()) {
                            // コロンなし12桁の場合はコロン区切りに整形し、大文字に統一
                            val formattedMac = if (!rawMac.contains(":") && rawMac.length == 12) {
                                rawMac.chunked(2).joinToString(":")
                            } else {
                                rawMac
                            }.uppercase()

                            Log.d(TAG, "getSavedBleDeviceAddress: server_config.txt からBLE MACを取得 = $formattedMac")
                            return formattedMac
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "getSavedBleDeviceAddress: server_config.txt 読み込みエラー", e)
            }
        }

        // フォールバック（従来のSharedPreferences）
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_SAVED_BLE_ADDRESS, null)
    }

    fun getConfiguredDeviceMeisho(context: Context): String? {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_SAVED_HOZON_NM, null)
    }

    fun saveBleDeviceAddress(context: Context, address: String, hozonNm: String? = null) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().apply {
            putString(KEY_SAVED_BLE_ADDRESS, address)
            if (hozonNm != null) {
                putString(KEY_SAVED_HOZON_NM, hozonNm)
            }
        }.commit()
    }

    fun hasSavedDevice(context: Context): Boolean {
        return !getSavedBleDeviceAddress(context).isNullOrEmpty()
    }

    /**
     * 外部の server_config.txt から通信先サーバーURLを動的に取得する。
     */
    fun getServerUrl(context: Context): String {
        val configFile = getConfigFile(context) ?: return ""

        return try {
            val content = configFile.readText().trim()

            // コメント行（# や ;）と BLE_MAC= 行を除外し、有効な最初の行をURLとして抽出
            val line = content.lines()
                .map { it.trim() }
                .firstOrNull {
                    it.isNotEmpty() &&
                            !it.startsWith("#") &&
                            !it.startsWith(";") &&
                            !it.uppercase().startsWith("BLE_MAC=")
                } ?: ""

            if (line.isNotEmpty()) {
                val formattedUrl = if (line.endsWith("/")) line else "$line/"
                Log.d(TAG, "getServerUrl: 取得したサーバーURL = $formattedUrl")
                formattedUrl
            } else {
                Log.w(TAG, "getServerUrl: 有効なURL行が見つかりませんでした")
                ""
            }
        } catch (e: Exception) {
            Log.e(TAG, "getServerUrl: ファイル読み込み中に例外発生", e)
            ""
        }
    }

    /**
     * server_config.txt の File オブジェクトを取得（なければデフォルト生成）
     */
    private fun getConfigFile(context: Context): File? {
        val filesDir = context.getExternalFilesDir(null)
        if (filesDir == null) {
            Log.e(TAG, "getExternalFilesDir が利用できません")
            return null
        }

        if (!filesDir.exists()) {
            filesDir.mkdirs()
        }

        val configFile = File(filesDir, "server_config.txt")
        if (!configFile.exists()) {
            try {
                configFile.writeText(
                    "http://192.168.0.12:8080/\n\n" +
                            "# バーコードリーダーのMACアドレス\n" +
                            "BLE_MAC="
                )
                Log.i(TAG, "server_config.txt が存在しないため、デフォルトファイルを自動生成しました: ${configFile.absolutePath}")
            } catch (e: Exception) {
                Log.e(TAG, "server_config.txt の自動生成に失敗しました", e)
            }
        }
        return configFile
    }
}