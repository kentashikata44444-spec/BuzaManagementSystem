package com.example.buzaimanagementsystem.ui.theme.skl0111g01

import android.app.Application
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.buzaimanagementsystem.utils.FileLogger
import kotlinx.coroutines.launch

class SKL0111G01ViewModel(
    application: Application,
    private val apiService: SKL0111G01ApiService? = null
) : AndroidViewModel(application) {

    companion object {
        private const val TAG = "SKL0111G01ViewModel"
    }

    // 画面初期化用パラメータ
    var userCd by mutableStateOf("")
        private set
    var gymTntCd by mutableStateOf("")
        private set
    var gymTntMei by mutableStateOf("")
        private set
    var sourceDivision by mutableStateOf("")
        private set
    var seihinBango by mutableStateOf("")
        private set

    // 画面表示用の状態（保管エリア・保管資材・メッセージ）
    var hokanAreaName by mutableStateOf("")
        private set
    var hokanShizaiName by mutableStateOf("")
        private set
    var areaMessage by mutableStateOf("")
        private set
    var shizaiMessage by mutableStateOf("")
        private set

    // 登録件数（0件の場合は「メニュー」有効・「確定」無効、1件以上の場合は逆）
    var registeredCount by mutableStateOf(0)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set
    var infoMessage by mutableStateOf<String?>(null)
        private set

    /**
     * 画面初期化パラメータの受け取り
     */
    fun initParams(
        userCd: String,
        gymTntCd: String,
        gymTntMei: String,
        sourceDivision: String,
        seihinBango: String
    ) {
        this.userCd = userCd
        this.gymTntCd = gymTntCd
        this.gymTntMei = gymTntMei
        this.sourceDivision = sourceDivision
        this.seihinBango = seihinBango

        writeLog("I", "initParams完了 -> userCd=$userCd, gymTntCd=$gymTntCd, sourceDivision=$sourceDivision, seihinBango=$seihinBango")

        // データの読み込み（現在の登録件数やエリア・資材情報の取得）
        fetchHokanData()
    }

    /**
     * バックエンドから保管エリア・保管資材、および仕分け登録件数を取得する処理
     */
    private fun fetchHokanData() {
        viewModelScope.launch {
            try {
                // TODO: 実際のAPI呼び出しに置き換えてください
                // val response = apiService?.getHokanInfo(seihinBango)
                // if (response != null && response.isSuccessful && response.body() != null) {
                //     val data = response.body()!!
                //     registeredCount = data.count
                //     hokanAreaName = data.areaName ?: ""
                //     hokanShizaiName = data.shizaiName ?: ""
                // } else {
                //     registeredCount = 0 // デモ・初期状態
                // }

                // 【検証用モック】必要に応じて書き換えてください
                registeredCount = 0
            } catch (e: Exception) {
                writeLog("E", "保管情報の取得に失敗しました", e)
                errorMessage = "システム管理者に連絡してください"
            }
        }
    }

    /**
     * ボタンの多重押下防止やクリック時の共通処理
     */
    fun onButtonClicked(action: () -> Unit) {
        errorMessage = null
        infoMessage = null
        action()
    }

    /**
     * 一時保管（保管エリア確定）ボタン押下時の処理
     * 仕様：仕分け登録が1件以上の場合のみ有効
     */
    fun executeHokanAreaKakutei(onSuccessNavigation: () -> Unit) {
        if (registeredCount <= 0) {
            errorMessage = "仕分け登録データがありません"
            return
        }

        viewModelScope.launch {
            try {
                writeLog("I", "一時保管(保管エリア確定) 処理開始: seihinBango=$seihinBango")

                // TODO: 必要であればエリア確定のAPIをここで実行する
                // val response = apiService?.confirmHokanArea(...)

                infoMessage = "保管エリアを確定しました"
                writeLog("I", "一時保管(保管エリア確定) 成功")

                // 成功時の画面遷移を実行
                onSuccessNavigation()
            } catch (e: Exception) {
                writeLog("E", "一時保管(保管エリア確定) エラー", e)
                errorMessage = "システム管理者に連絡してください"
            }
        }
    }

    private fun writeLog(level: String, message: String, throwable: Throwable? = null) {
        val context = getApplication<Application>()
        when (level) {
            "D" -> Log.d(TAG, message, throwable)
            "W" -> Log.w(TAG, message, throwable)
            "E" -> Log.e(TAG, message, throwable)
            "I" -> Log.i(TAG, message, throwable)
        }
        FileLogger.writeLog(context, level, TAG, message)
    }
}