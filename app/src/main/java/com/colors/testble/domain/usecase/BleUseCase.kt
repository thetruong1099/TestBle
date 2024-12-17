package com.colors.testble.domain.usecase

data class BleUseCase(
    val getBleDeviceUseCase: GetBleDeviceUseCase,
    val scanBleUseCase: ScanBleUseCase,
    val stopScanBleUseCase: StopScanBleUseCase,
)
