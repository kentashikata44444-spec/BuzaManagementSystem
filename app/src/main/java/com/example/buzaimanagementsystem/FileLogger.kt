package com.example.buzaimanagementsystem.utils

import android.content.Context
import android.util.Log
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object FileLogger {
    private const val TAG = "FileLogger"
    private const val LOG_FILE_NAME = "app_debug_log.txt"

    /**
     * ログをファイルに追記する
     */
    fun writeLog(context: Context, level: String, tag: String, message: String) {
        try {
            val timeStamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.JAPAN).format(Date())
            val logEntry = "[$timeStamp] [$level] [$tag]: $message\n"

            // アプリの内部ストレージ（filesDir）に保存する場合
            val logFile = File(context.filesDir, LOG_FILE_NAME)
            logFile.appendText(logEntry)

        } catch (e: Exception) {
            Log.e(TAG, "ログのファイル書き込みに失敗しました", e)
        }
    }

    /**
     * 保存されたログファイルのパスを取得する（確認用）
     */
    fun getLogFilePath(context: Context): String {
        return File(context.filesDir, LOG_FILE_NAME).absolutePath
    }

    /**
     * ログファイルを読み込む（画面表示や共有用）
     */
    fun readLog(context: Context): String {
        return try {
            val logFile = File(context.filesDir, LOG_FILE_NAME)
            if (logFile.exists()) logFile.readText() else "ログファイルはまだありません。"
        } catch (e: Exception) {
            "ログの読み込みに失敗しました: ${e.message}"
        }
    }

    /**
     * ログをクリアする
     */
    fun clearLog(context: Context) {
        try {
            val logFile = File(context.filesDir, LOG_FILE_NAME)
            if (logFile.exists()) {
                logFile.delete()
            }
        } catch (e: Exception) {
            Log.e(TAG, "ログファイルの削除に失敗しました", e)
        }
    }
}