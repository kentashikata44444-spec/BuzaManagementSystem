package com.example.buzaimanagementsystem.utils

import android.content.Context
import java.net.NetworkInterface
import java.util.Collections

data class DeviceMapping(
    val targetIpAddress: String,   // Android端末のIPアドレス（例: "192.168.1.50"）
    val targetMacAddress: String?, // 紐づけるバーコードリーダーのMACアドレス
    val targetMeisho: String?      // 紐づけるバーコードリーダーの銘称（HOZON_NM or MEISHO）
)

object AppConfig {
    private const val PREF_NAME = "BuzaManagementAppConfig"
    private const val KEY_SAVED_BLE_ADDRESS = "saved_ble_address"
    private const val KEY_SAVED_HOZON_NM = "saved_hozon_nm"

    /**
     * 【IPアドレスごとの紐づけ設定リスト】
     * 現場のAndroid端末のIPアドレスごとに、紐づけるバーコードリーダーの
     * MACアドレスまたは銘称をここに書き込んでおきます。
     */
    private val PRECONFIGURED_DEVICES = listOf(
        // 例1: IPアドレスが "192.168.137.89" のタブレットの場合
        DeviceMapping(
            targetIpAddress = "192.168.3.48",
            targetMacAddress = "DC:0D:30:13:05:26",
            targetMeisho = "EY-017ZP"
        ),
        // 例2: IPアドレスが "192.168.137.48" のタブレットの場合
        DeviceMapping(
            targetIpAddress = "192.168.137.48",
            targetMacAddress = "DC:0D:30:13:05:26",
            targetMeisho = "EY-017ZP"
        )
    )

    /**
     * 端末自身のローカルIPアドレス（IPv4）を取得する
     */
    private fun getLocalIpAddress(): String? {
        try {
            val interfaces = Collections.list(NetworkInterface.getNetworkInterfaces())
            for (intf in interfaces) {
                val addrs = Collections.list(intf.inetAddresses)
                for (addr in addrs) {
                    if (!addr.isLoopbackAddress) {
                        val sAddr = addr.hostAddress ?: continue
                        // IPv4アドレスのみを対象とする
                        val isIPv4 = sAddr.indexOf(':') < 0
                        if (isIPv4) {
                            return sAddr
                        }
                    }
                }
            }
        } catch (ignored: Exception) {
        }
        return null
    }

    /**
     * 現在のAndroid端末のIPアドレスに対応する設定情報を取得する
     */
    fun getConfiguredDeviceForCurrentTerminal(): DeviceMapping? {
        val currentIp = getLocalIpAddress() ?: return null

        // リストの中から、現在の端末のIPアドレスに一致する設定を探す
        return PRECONFIGURED_DEVICES.find {
            it.targetIpAddress == currentIp
        }
    }

    fun getSavedBleDeviceAddress(context: Context): String? {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val saved = prefs.getString(KEY_SAVED_BLE_ADDRESS, null)
        if (!saved.isNullOrEmpty()) return saved

        // 保存されていなければ、IPアドレスベースのハードコード設定から自動で取得して返す
        return getConfiguredDeviceForCurrentTerminal()?.targetMacAddress
    }

    fun getConfiguredDeviceMeisho(context: Context): String? {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val saved = prefs.getString(KEY_SAVED_HOZON_NM, null)
        if (!saved.isNullOrEmpty()) return saved

        return getConfiguredDeviceForCurrentTerminal()?.targetMeisho
    }

    fun saveBleDeviceAddress(context: Context, address: String, hozonNm: String? = null) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        // apply() の代わりに commit() を使用して古いAPIレベルに対応
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