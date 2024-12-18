package com.colors.testble.domain.usecase

data class BleUseCase(
    val getBleDeviceUseCase: GetBleDeviceUseCase,
    val scanBleUseCase: ScanBleUseCase,
    val stopScanBleUseCase: StopScanBleUseCase,
    val startGattServerUseCase: StartGattServerUseCase,
    val connectToDeviceUseCase: ConnectToDeviceUseCase,
    val disconnectUSeCase: DisconnectUseCase,
    val writeCharacteristicUseCase: WriteCharacteristicUseCase,
)
