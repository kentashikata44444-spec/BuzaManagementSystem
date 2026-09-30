package com.example.buzaimanagementsystem.utils

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

fun performLogoutAndExit(
    coroutineScope: CoroutineScope,
    apiService: CommonApiService, // ".kt" を削除して型名だけにする
    userCd: String,
    onExit: () -> Unit
) {
    coroutineScope.launch {
        try {
            if (userCd.isNotEmpty()) {
                // サーバー側の共通削除APIを叩く
                apiService.deleteLogin(userCd)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            // 処理が終わったらアプリ終了
            onExit()
        }
    }
}