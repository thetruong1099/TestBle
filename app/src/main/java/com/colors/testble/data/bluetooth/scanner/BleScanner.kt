package com.colors.testble.data.bluetooth.scanner

import android.bluetooth.BluetoothDevice
import kotlinx.coroutines.flow.Flow

interface BleScanner {
    val scanResults: Flow<List<BluetoothDevice>>
    fun startScanning()
    fun stopScanning()
}