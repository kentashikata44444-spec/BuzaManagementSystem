package com.example.buzaimanagementsystem.utils

import android.content.Context
import android.util.Log
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object FileLogger {
    private const val TAG = "FileLogger"

    /**
     * 日付を含んだファイル名を動的に生成する
     */
    private fun getLogFileName(): String {
        val dateString = SimpleDateFormat("yyyy-MM-dd", Locale.JAPAN).format(Date())
        return "app_debug_log_$dateString.txt"
    }

    /**
     * ログをファイルに追記する
     */
    fun writeLog(context: Context, level: String, tag: String, message: String) {
        try {
            val timeStamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.JAPAN).format(Date())
            val logEntry = "[$timeStamp] [$level] [$tag]: $message\n"

            val logFile = File(context.filesDir, getLogFileName())
            logFile.appendText(logEntry)

        } catch (e: Exception) {
            Log.e(TAG, "ログのファイル書き込みに失敗しました", e)
        }
    }

    /**
     * 保存されたログファイルのパスを取得する（確認用）
     */
    fun getLogFilePath(context: Context): String {
        return File(context.filesDir, getLogFileName()).absolutePath
    }

    /**
     * ログファイルを読み込む（画面表示や共有用）
     */
    fun readLog(context: Context): String {
        return try {
            val logFile = File(context.filesDir, getLogFileName())
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
            val logFile = File(context.filesDir, getLogFileName())
            if (logFile.exists()) {
                logFile.delete()
            }
        } catch (e: Exception) {
            Log.e(TAG, "ログファイルの削除に失敗しました", e)
        }
    }
}