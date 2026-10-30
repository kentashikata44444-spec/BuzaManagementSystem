package com.example.buzaimanagementsystem.utils

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import java.util.UUID

@SuppressLint("NewApi", "LogTagMismatch")
object BluetoothConnectionHelper {

    private const val TAG = "BtConnectionHelper"
    private var bluetoothGatt: BluetoothGatt? = null
    private var isConnectedState = false

    // 連続して接続要求がスパムのように走るのを防ぐためのフラグ
    private var isConnecting = false

    private val handler = Handler(Looper.getMainLooper())

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
        if (!isBluetoothEnabled()) {
            isConnectedState = false
            return false
        }
        if (savedAddress.isNullOrEmpty()) return false

        val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager ?: return false
        try {
            val connectedDevices = bluetoothManager.getConnectedDevices(BluetoothProfile.GATT)
            for (device in connectedDevices) {
                if (device.address.equals(savedAddress, ignoreCase = true)) {
                    isConnectedState = true
                    return true
                }
            }
        } catch (e: Exception) {
            writeLog(context, "E", "isBleConnected: 例外発生", e)
        }

        isConnectedState = false
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
        onConnectionStateChanged: (Boolean, String?) -> Unit,
        onDetailedError: ((String) -> Unit)? = null,
        onDataReceived: ((String) -> Unit)? = null // バーコードデータ受信用コールバックを追加
    ) {
        if (isConnecting) {
            writeLog(context, "W", "connectToSavedDevice: すでに接続処理中のためスキップします")
            return
        }
        isConnecting = true

        writeLog(context, "I", "connectToSavedDevice: 呼び出し開始（詳細切り分け）")

        val savedAddress = getSavedBleDeviceAddress(context)
        if (savedAddress.isNullOrEmpty()) {
            isConnecting = false
            writeLog(context, "W", "connectToSavedDevice: 保存されたBLEアドレスがありません")
            onDetailedError?.invoke("警告: 保存されたBLEアドレスがありません")
            onConnectionStateChanged(false, null)
            return
        }

        if (!isBluetoothEnabled()) {
            isConnecting = false
            writeLog(context, "W", "connectToSavedDevice: BluetoothがOFFです")
            onDetailedError?.invoke("エラー: BluetoothがOFFになっています")
            onConnectionStateChanged(false, null)
            return
        }

        try {
            val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager ?: return
            val bluetoothAdapter = bluetoothManager.adapter ?: return

            val device = try {
                bluetoothAdapter.getRemoteDevice(savedAddress)
            } catch (e: Exception) {
                null
            }

            if (device == null) {
                isConnecting = false
                onConnectionStateChanged(false, savedAddress)
                return
            }

            handler.post {
                try {
                    if (bluetoothGatt != null) {
                        try { bluetoothGatt?.disconnect() } catch (_: Exception) {}
                        try { bluetoothGatt?.close() } catch (_: Exception) {}
                        bluetoothGatt = null
                    }
                    isConnectedState = false

                    var callbackTriggered = false

                    bluetoothGatt = device.connectGatt(
                        context,
                        false,
                        object : BluetoothGattCallback() {
                            override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
                                super.onConnectionStateChange(gatt, status, newState)
                                callbackTriggered = true
                                isConnecting = false
                                writeLog(context, "I", "onConnectionStateChange: status=$status, newState=$newState")

                                if (status == 8 || (status == BluetoothGatt.GATT_SUCCESS && newState == BluetoothProfile.STATE_CONNECTED)) {
                                    writeLog(context, "I", "接続OK。サービス探索を開始します")
                                    isConnectedState = true
                                    onConnectionStateChanged(true, savedAddress)
                                    // 接続成功したらGATTサービスを探索する
                                    gatt?.discoverServices()
                                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                                    writeLog(context, "W", "デバイスが切断されました (STATE_DISCONNECTED)")
                                    isConnectedState = false
                                    onDetailedError?.invoke("ハードウェア異常 システム管理者に問合せ願います")
                                    onConnectionStateChanged(false, savedAddress)
                                } else {
                                    isConnecting = false
                                    writeLog(context, "W", "接続エラー status=$status, newState=$newState")
                                    onDetailedError?.invoke("ハードウェア異常 システム管理者に問合せ願います")
                                    isConnectedState = false
                                    onConnectionStateChanged(false, savedAddress)
                                }
                            }

                            override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
                                super.onServicesDiscovered(gatt, status)
                                if (status == BluetoothGatt.GATT_SUCCESS && gatt != null) {
                                    writeLog(context, "I", "GATTサービス探索成功。通知設定を構成します")
                                    // バーコードリーダーのデータ通知(Notification)を有効化
                                    for (service in gatt.services) {
                                        for (characteristic in service.characteristics) {
                                            val properties = characteristic.properties
                                            if ((properties and android.bluetooth.BluetoothGattCharacteristic.PROPERTY_NOTIFY) != 0 ||
                                                (properties and android.bluetooth.BluetoothGattCharacteristic.PROPERTY_INDICATE) != 0) {

                                                gatt.setCharacteristicNotification(characteristic, true)

                                                // Client Characteristic Configuration Descriptor (CCCD) の書込み
                                                val descriptorUuid = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")
                                                val descriptor = characteristic.getDescriptor(descriptorUuid)
                                                if (descriptor != null) {
                                                    descriptor.value = if ((properties and android.bluetooth.BluetoothGattCharacteristic.PROPERTY_NOTIFY) != 0) {
                                                        BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                                                    } else {
                                                        BluetoothGattDescriptor.ENABLE_INDICATION_VALUE
                                                    }
                                                    gatt.writeDescriptor(descriptor)
                                                    writeLog(context, "I", "通知設定を書き込みました: ${characteristic.uuid}")
                                                }
                                            }
                                        }
                                    }
                                } else {
                                    writeLog(context, "W", "GATTサービス探索失敗: status=$status")
                                }
                            }

                            override fun onCharacteristicChanged(
                                gatt: BluetoothGatt,
                                characteristic: android.bluetooth.BluetoothGattCharacteristic
                            ) {
                                super.onCharacteristicChanged(gatt, characteristic)
                                val dataBytes = characteristic.value
                                if (dataBytes != null && dataBytes.isNotEmpty()) {
                                    val scannedText = String(dataBytes, Charsets.UTF_8).trim()
                                    writeLog(context, "I", "バーコードデータ受信: $scannedText")

                                    // メインスレッドでデータ受信コールバックを呼び出し
                                    handler.post {
                                        onDataReceived?.invoke(scannedText)
                                    }
                                }
                            }
                        },
                        BluetoothDevice.TRANSPORT_LE
                    )

                    handler.postDelayed({
                        if (!callbackTriggered && !isConnectedState) {
                            isConnecting = false
                            writeLog(context, "W", "タイムアウト -> バーコードリーダーの電源がOFF")
                            onDetailedError?.invoke("ハードウェア異常 システム管理者に問合せ願います")
                            try {
                                bluetoothGatt?.disconnect()
                                bluetoothGatt?.close()
                            } catch (_: Exception) {}
                            bluetoothGatt = null
                            onConnectionStateChanged(false, savedAddress)
                        }
                    }, 3000L)

                } catch (e: Exception) {
                    isConnecting = false
                    writeLog(context, "E", "connectToSavedDevice: 実行時例外", e)
                    isConnectedState = false
                    onConnectionStateChanged(false, savedAddress)
                }
            }
        } catch (e: Exception) {
            isConnecting = false
            writeLog(context, "E", "connectToSavedDevice: 例外発生", e)
            onConnectionStateChanged(false, savedAddress)
        }
    }
}