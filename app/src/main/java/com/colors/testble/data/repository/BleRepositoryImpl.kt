package com.colors.testble.data.repository

import com.colors.testble.data.bluetooth.connection.BleConnectionManager
import com.colors.testble.data.bluetooth.scanner.BleScanner
import com.colors.testble.domain.repository.BleRepository

class BleRepositoryImpl(
    private val bleScanner: BleScanner,
    private val bleConnectionManager: BleConnectionManager
) : BleRepository {
    override fun getScannedDeviceList() = bleScanner.scanResults

    override fun startScanning() {
        bleScanner.startScanning()
    }

    override fun stopScanning() {
        bleScanner.stopScanning()
    }

    override fun startServer() {
        bleConnectionManager.startServer()
    }

    override fun stopServer() {
        bleConnectionManager.stopServer()
    }

    override fun connectToDevice(address: String) {
        bleConnectionManager.connect(address)
    }

    override fun disconnect() {
        bleConnectionManager.disconnect()
    }

    override fun writeCharacteristic(message: String) {
        bleConnectionManager.writeCharacteristic(message)
    }
}
