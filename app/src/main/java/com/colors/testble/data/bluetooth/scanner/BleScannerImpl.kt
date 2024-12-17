package com.colors.testble.data.bluetooth.scanner

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.os.ParcelUuid
import com.colors.testble.data.utils.SERVICE_UUID
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow

@SuppressLint("MissingPermission")
class BleScannerImpl(
    private val bluetoothLeScanner: BluetoothLeScanner,
    private val deviceList: MutableMap<String, BluetoothDevice> = mutableMapOf(),
    private val channel: Channel<List<BluetoothDevice>> = Channel(Channel.BUFFERED),
    private val close: (Throwable) -> Unit = { throwable -> channel.close(throwable) },
    private val settings: ScanSettings = ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_POWER).build(),
) : ScanCallback(),
    BleScanner {
    override val scanResults: Flow<List<BluetoothDevice>> = channel.receiveAsFlow()

    override fun onScanResult(
        callbackType: Int,
        result: ScanResult?,
    ) {
        super.onScanResult(callbackType, result)
        result?.let {
            deviceList[it.device.address] = it.device
            trySend(deviceList.values.toList())
        }
    }

    private fun trySend(results: List<BluetoothDevice>) {
        channel.trySend(results).isSuccess
    }

    override fun startScanning() {
        bluetoothLeScanner.startScan(null, settings, this)
    }

    override fun stopScanning() {
        bluetoothLeScanner.stopScan(this)
    }

    /**
     * Return a List of [ScanFilter] objects to filter by Service UUID.
     */
    private fun buildScanFilters(): List<ScanFilter> {
        val builder = ScanFilter.Builder()
        // Comment out the below line to see all BLE devices around you
        builder.setServiceUuid(ParcelUuid(SERVICE_UUID))
        val filter = builder.build()
        return listOf(filter)
    }
}
