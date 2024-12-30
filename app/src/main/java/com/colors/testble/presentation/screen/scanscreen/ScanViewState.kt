package com.colors.testble.presentation.screen.scanscreen

import android.bluetooth.BluetoothDevice
import com.colors.testble.presentation.base.IViewState

data class ScanViewState(
    val scannedDevices: List<BluetoothDevice> = emptyList()
) : IViewState
