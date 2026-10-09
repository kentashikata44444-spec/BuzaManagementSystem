package com.example.buzaimanagementsystem.utils

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.content.Context
import android.util.Log

object BluetoothConnectionHelper {

    private const val TAG = "BluetoothConnectionHelper"
    private var bluetoothGatt: BluetoothGatt? = null

    private fun writeLog(context: Context, level: String, message: String, throwable: Throwable? = null) {
        when (level) {
            "D" -> Log.d(TAG, message, throwable)
            "W" -> Log.w(TAG, message, throwable)
            "E" -> Log.e(TAG, message, throwable)
            "I" -> Log.i(TAG, message, throwable)
        }
        FileLogger.writeLog(context, level, TAG, message + (throwable?.let { " : ${it.message}" } ?: ""))
    }

    fun isBluetoothEnabled(): Boolean {
        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter() ?: return false
        return bluetoothAdapter.isEnabled
    }

    @SuppressLint("MissingPermission")
    fun isBleConnected(context: Context, savedAddress: String?): Boolean {
        if (!isBluetoothEnabled()) return false
        if (savedAddress.isNullOrEmpty()) return false

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            if (androidx.core.content.ContextCompat.checkSelfPermission(
                    context,
                    android.Manifest.permission.BLUETOOTH_CONNECT
                ) != android.content.pm.PackageManager.PERMISSION_GRANTED
            ) {
                return false
            }
        }

        val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager ?: return false

        try {
            val connectedDevices = bluetoothManager.getConnectedDevices(BluetoothProfile.GATT)
            for (device in connectedDevices) {
                if (device.address == savedAddress) return true
            }
        } catch (e: Exception) {
            writeLog(context, "E", "isBleConnected: 例外発生", e)
        }
        return false
    }

    fun getSavedBleDeviceAddress(context: Context): String? {
        return AppConfig.getSavedBleDeviceAddress(context)
    }

    fun saveBleDeviceAddress(context: Context, address: String) {
        AppConfig.saveBleDeviceAddress(context, address)
    }

    @SuppressLint("MissingPermission")
    fun connectToSavedDevice(
        context: Context,
        onConnectionStateChanged: (Boolean, String?) -> Unit
    ) {
        writeLog(context, "I", "connectToSavedDevice: 呼び出し開始")

        if (!isBluetoothEnabled()) {
            writeLog(context, "W", "connectToSavedDevice: BluetoothがOFFです")
            onConnectionStateChanged(false, null)
            return
        }

        val savedAddress = getSavedBleDeviceAddress(context)
        if (savedAddress.isNullOrEmpty()) {
            writeLog(context, "W", "connectToSavedDevice: 保存されたBLEアドレスがありません")
            onConnectionStateChanged(false, null)
            return
        }

        writeLog(context, "I", "connectToSavedDevice: 対象アドレス = $savedAddress")

        try {
            val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
            if (bluetoothManager == null) {
                writeLog(context, "E", "connectToSavedDevice: BluetoothManagerが取得できません")
                onConnectionStateChanged(false, savedAddress)
                return
            }

            // ★ すでに接続されている場合は、無駄な connectGatt を呼ばずに即座に成功を返す
            if (isBleConnected(context, savedAddress)) {
                writeLog(context, "I", "connectToSavedDevice: すでにOS側で接続されています")
                onConnectionStateChanged(true, savedAddress)
                return
            }

            val bluetoothAdapter = bluetoothManager.adapter
            if (bluetoothAdapter == null) {
                writeLog(context, "E", "connectToSavedDevice: BluetoothAdapterが取得できません")
                onConnectionStateChanged(false, savedAddress)
                return
            }

            val device = try {
                bluetoothAdapter.getRemoteDevice(savedAddress)
            } catch (e: IllegalArgumentException) {
                writeLog(context, "E", "connectToSavedDevice: 不正なMACアドレス: $savedAddress", e)
                null
            }

            if (device == null) {
                writeLog(context, "E", "connectToSavedDevice: BluetoothDeviceの取得に失敗しました")
                onConnectionStateChanged(false, savedAddress)
                return
            }

            // 古いGattのセッションを完全に閉じる
            try {
                bluetoothGatt?.disconnect()
                bluetoothGatt?.close()
            } catch (_: Exception) {}
            bluetoothGatt = null

            writeLog(context, "I", "connectToSavedDevice: device.connectGatt を実行します...")

            // 第2引数の autoConnect を false にしていると、電波状況や既存セッションの残骸で 147 (Timeout) になりやすい。
            // バーコードリーダーのような周辺機器は autoConnect = false のままだとシビアなため、
            // もし頻発する場合は自動再接続（autoConnect = true）にするか、既存接続の解放を確実に行う必要があります。
            // ここではダイレクト接続のまま、コールバックでのステータス制御を堅牢にします。
            bluetoothGatt = device.connectGatt(
                context,
                false,
                object : BluetoothGattCallback() {
                    override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
                        super.onConnectionStateChange(gatt, status, newState)
                        writeLog(context, "I", "onConnectionStateChange: status=$status, newState=$newState")

                        if (status != BluetoothGatt.GATT_SUCCESS) {
                            writeLog(context, "E", "onConnectionStateChange: エラー発生 status=$status")
                            try { gatt?.close() } catch (_: Exception) {}
                            if (bluetoothGatt == gatt) bluetoothGatt = null
                            onConnectionStateChanged(false, savedAddress)
                            return
                        }

                        when (newState) {
                            BluetoothProfile.STATE_CONNECTED -> {
                                writeLog(context, "I", "onConnectionStateChange: 接続成功")
                                // 接続成功したらサービスディスカバリーを走らせるか、そのままコールバックを返す
                                onConnectionStateChanged(true, savedAddress)
                            }
                            BluetoothProfile.STATE_DISCONNECTED -> {
                                writeLog(context, "I", "onConnectionStateChange: 切断されました")
                                try { gatt?.close() } catch (_: Exception) {}
                                if (bluetoothGatt == gatt) bluetoothGatt = null
                                onConnectionStateChanged(false, savedAddress)
                            }
                        }
                    }
                },
                BluetoothDevice.TRANSPORT_LE
            )

            if (bluetoothGatt == null) {
                writeLog(context, "E", "connectToSavedDevice: connectGatt の戻りが null です")
                onConnectionStateChanged(false, savedAddress)
            }

        } catch (e: Exception) {
            writeLog(context, "E", "connectToSavedDevice: 例外が発生しました", e)
            onConnectionStateChanged(false, savedAddress)
        }
    }

    @SuppressLint("MissingPermission")
    fun disconnect() {
        try {
            bluetoothGatt?.disconnect()
            bluetoothGatt?.close()
        } catch (_: Exception) {}
        bluetoothGatt = null
    }
}