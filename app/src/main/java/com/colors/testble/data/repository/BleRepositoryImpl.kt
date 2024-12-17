package com.colors.testble.data.repository

import com.colors.testble.data.bluetooth.scanner.BleScanner
import com.colors.testble.domain.repository.BleRepository

class BleRepositoryImpl(
    private val bleScanner: BleScanner,
) : BleRepository {
    override fun getScannedDeviceList() = bleScanner.scanResults

    override fun startScanning() {
        bleScanner.startScanning()
    }

    override fun stopScanning() {
        bleScanner.stopScanning()
    }
}
