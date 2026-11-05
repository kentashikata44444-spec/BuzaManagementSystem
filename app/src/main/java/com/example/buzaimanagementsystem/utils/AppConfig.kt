package com.example.buzaimanagementsystem.utils

import android.content.Context
import android.util.Log
import java.io.File

object AppConfig {
    private const val TAG = "AppConfig"

    private const val PREF_NAME = "BuzaManagementAppConfig"
    private const val KEY_SAVED_BLE_ADDRESS = "saved_ble_address"
    private const val KEY_SAVED_HOZON_NM = "saved_hozon_nm"

    fun getSavedBleDeviceAddress(context: Context): String? {
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
     * ファイルやディレクトリが存在しない場合は、デフォルト値で自動生成する。
     * （権限不要で、PCやファイルマネージャーから Android/data/com.example.buzaimanagementsystem/files/ に配置可能）
     */
    fun getServerUrl(context: Context): String {
        // アプリ専用外部ストレージの files ディレクトリを取得（権限不要）
        val filesDir = context.getExternalFilesDir(null)
        if (filesDir == null) {
            Log.e(TAG, "getExternalFilesDir が利用できません")
            return ""
        }

        val configFile = File(filesDir, "server_config.txt")
        Log.d(TAG, "getServerUrl: 探索パス = ${configFile.absolutePath}")

        // フォルダが存在しない場合は自動作成
        if (!filesDir.exists()) {
            filesDir.mkdirs()
        }

        // ファイルが存在しない場合は、デフォルトのURLで自動生成する
        if (!configFile.exists()) {
            try {
                // デフォルトの接続先URLを書き込んでおく
                configFile.writeText("http://192.168.137.99:8080/")
                Log.i(TAG, "server_config.txt が存在しないため、デフォルトファイルを自動生成しました: ${configFile.absolutePath}")
            } catch (e: Exception) {
                Log.e(TAG, "server_config.txt の自動生成に失敗しました", e)
            }
        }

        return try {
            val content = configFile.readText().trim()

            // コメント行（# や ;）を除外し、有効な最初の行をURLとして抽出
            val line = content.lines()
                .map { it.trim() }
                .firstOrNull { it.isNotEmpty() && !it.startsWith("#") && !it.startsWith(";") }
                ?: ""

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
}