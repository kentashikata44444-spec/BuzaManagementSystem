package com.example.buzaimanagementsystem.utils

import android.content.Context

object AppConfig {
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
}