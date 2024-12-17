package com.colors.testble.presentation.activity

import android.bluetooth.BluetoothDevice
import com.colors.testble.presentation.base.IViewState

data class MainViewState(
    val scannedDevices: List<BluetoothDevice> = emptyList(),
) : IViewState
