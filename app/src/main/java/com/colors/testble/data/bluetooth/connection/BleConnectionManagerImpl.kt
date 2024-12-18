package com.colors.testble.data.bluetooth.connection

import android.annotation.SuppressLint
import android.app.Application
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import com.colors.testble.data.bluetooth.server.BLEServer

class BleConnectionManagerImpl(
    private val application: Application,
    private val bluetoothAdapter: BluetoothAdapter,
    private val bluetoothManager: BluetoothManager,
) : BleConnectionManager {

    override fun startServer() {
        BLEServer.startServer(application, bluetoothAdapter, bluetoothManager)
    }

    override fun stopServer() {
        BLEServer.stopServer()
    }

    @SuppressLint("MissingPermission")
    override fun connect(address: String) {
        BLEServer.connect(address)
    }

    override fun disconnect() {
        BLEServer.disconnect()
    }

    @SuppressLint("MissingPermission")
    override fun writeCharacteristic(message: String) {
        BLEServer.writeCharacteristic(message)
    }
}