package com.example.buzaimanagementsystem.utils

import android.content.Context
import android.os.Environment
import android.util.Log
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object FileLogger {
    private const val TAG = "FileLogger"
    private const val FOLDER_NAME = "BuzaiManagementSystem"

    /**
     * 日付を含んだファイル名を動的に生成する
     */
    private fun getLogFileName(): String {
        val dateString = SimpleDateFormat("yyyy-MM-dd", Locale.JAPAN).format(Date())
        return "app_debug_log_$dateString.txt"
    }

    /**
     * 共有の Documents フォルダ内に専用の保存先ディレクトリを取得する
     */
    private fun getLogDirectory(): File {
        // 共有の Documents フォルダを取得
        val documentsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
        val targetDir = File(documentsDir, FOLDER_NAME)
        if (!targetDir.exists()) {
            targetDir.mkdirs()
        }
        return targetDir
    }

    /**
     * ログをファイルに追記する
     */
    fun writeLog(context: Context, level: String, tag: String, message: String) {
        try {
            val timeStamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.JAPAN).format(Date())
            val logEntry = "[$timeStamp] [$level] [$tag]: $message\n"

            val logFile = File(getLogDirectory(), getLogFileName())
            logFile.appendText(logEntry)

        } catch (e: Exception) {
            Log.e(TAG, "ログのファイル書き込みに失敗しました", e)
        }
    }

    /**
     * 保存されたログファイルのパスを取得する（確認用）
     */
    fun getLogFilePath(context: Context): String {
        return File(getLogDirectory(), getLogFileName()).absolutePath
    }

    /**
     * ログファイルを読み込む
     */
    fun readLog(context: Context): String {
        return try {
            val logFile = File(getLogDirectory(), getLogFileName())
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
            val logFile = File(getLogDirectory(), getLogFileName())
            if (logFile.exists()) {
                logFile.delete()
            }
        } catch (e: Exception) {
            Log.e(TAG, "ログファイルの削除に失敗しました", e)
        }
    }
}