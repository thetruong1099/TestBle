package com.colors.testble.presentation.activity

import android.bluetooth.BluetoothDevice
import com.colors.testble.data.local.entity.LogEntity
import com.colors.testble.presentation.base.IViewState

data class MainViewState(
    val scannedDevices: List<BluetoothDevice> = emptyList(),
    val logList: List<LogEntity> = emptyList()
) : IViewState
