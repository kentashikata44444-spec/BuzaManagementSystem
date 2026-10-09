package com.example.buzaimanagementsystem.utils

import android.content.Context

object AppConfig {
    private const val PREF_NAME = "BuzaManagementAppConfig"
    private const val KEY_SAVED_BLE_ADDRESS = "saved_ble_address"

    // ★テスト用の固定アドレス（必要に応じて自分のデバイスのアドレスに変更）
    private const val TEST_BLE_ADDRESS = "DC:0D:30:13:05:26"

    fun getSavedBleDeviceAddress(context: Context): String? {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val saved = prefs.getString(KEY_SAVED_BLE_ADDRESS, null)

        // 保存されていなければ、テスト用アドレスを返す（テストが終わったらこの行を消せます）
        if (saved.isNullOrEmpty()) {
            return TEST_BLE_ADDRESS
        }
        return saved
    }

    fun saveBleDeviceAddress(context: Context, address: String) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_SAVED_BLE_ADDRESS, address).apply()
    }
}