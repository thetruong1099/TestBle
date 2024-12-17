package com.colors.testble.domain.repository

import android.bluetooth.BluetoothDevice
import kotlinx.coroutines.flow.Flow

interface BleRepository {
    fun getScannedDeviceList(): Flow<List<BluetoothDevice>>
    fun startScanning()
    fun stopScanning()
}
