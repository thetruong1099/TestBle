package com.colors.testble.di

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeScanner
import android.content.Context
import com.colors.testble.data.bluetooth.scanner.BleScanner
import com.colors.testble.data.bluetooth.scanner.BleScannerImpl
import com.colors.testble.data.repository.BleRepositoryImpl
import com.colors.testble.domain.repository.BleRepository
import com.colors.testble.domain.usecase.BleUseCase
import com.colors.testble.domain.usecase.GetBleDeviceUseCase
import com.colors.testble.domain.usecase.ScanBleUseCase
import com.colors.testble.domain.usecase.StopScanBleUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
data object BleModule {
    @Provides
    @Singleton
    fun provideBluetoothManager(
        @ApplicationContext appContext: Context,
    ): BluetoothManager = appContext.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager

    @Provides
    @Singleton
    fun provideBluetoothAdapter(bluetoothManager: BluetoothManager): BluetoothAdapter = bluetoothManager.adapter

    @Provides
    @Singleton
    fun provideBluetoothLeScanner(bluetoothAdapter: BluetoothAdapter): BluetoothLeScanner =
        bluetoothAdapter.bluetoothLeScanner

    @Provides
    @Singleton
    fun provideBleScanner(bluetoothLeScanner: BluetoothLeScanner): BleScanner = BleScannerImpl(bluetoothLeScanner)

    @Provides
    @Singleton
    fun provideBleRepository(bleScanner: BleScanner): BleRepository = BleRepositoryImpl(bleScanner)

    @Provides
    @Singleton
    fun provideBleUseCase(bleRepository: BleRepository): BleUseCase =
        BleUseCase(
            getBleDeviceUseCase = GetBleDeviceUseCase(bleRepository),
            scanBleUseCase = ScanBleUseCase(bleRepository),
            stopScanBleUseCase = StopScanBleUseCase(bleRepository),
        )
}
