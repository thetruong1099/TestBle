package com.colors.testble.presentation.screen.scanscreen

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothProfile
import android.bluetooth.BluetoothStatusCodes
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.lifecycle.viewModelScope
import com.colors.testble.data.utils.RESPONSE_MESSAGE_UUID
import com.colors.testble.data.utils.SEND_MESSAGE_UUID
import com.colors.testble.data.utils.SERVICE_UUID
import com.colors.testble.domain.usecase.BleUseCase
import com.colors.testble.domain.usecase.GetBleDeviceUseCase
import com.colors.testble.domain.usecase.ScanBleUseCase
import com.colors.testble.domain.usecase.StopScanBleUseCase
import com.colors.testble.presentation.base.BaseViewModel
import com.colors.testble.presentation.base.IViewEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ScanViewModel @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val bluetoothAdapter: BluetoothAdapter,
    private val bleUseCase: BleUseCase,
) : BaseViewModel<ScanViewState, ScanViewEvent>() {
    private var bluetoothGatt: BluetoothGatt? = null
    private var messageCharacteristic: BluetoothGattCharacteristic? = null
    private var responseMessageCharacteristic: BluetoothGattCharacteristic? = null

    private val bluetoothGattCallback = object : BluetoothGattCallback() {
        @SuppressLint("MissingPermission")
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            super.onConnectionStateChange(gatt, status, newState)
            val isSuccess = status == BluetoothGatt.GATT_SUCCESS
            val isConnected = newState == BluetoothProfile.STATE_CONNECTED
            Log.d("devLog", "onConnectionStateChange: $isSuccess $isConnected")
            // try to send a message to the other device as a test
            if (isSuccess && isConnected) {
                // discover services
                gatt.discoverServices()
            }
        }

        @SuppressLint("MissingPermission")
        override fun onServicesDiscovered(discoveredGatt: BluetoothGatt, status: Int) {
            super.onServicesDiscovered(discoveredGatt, status)
            if (status == BluetoothGatt.GATT_SUCCESS) {
                val service = discoveredGatt.getService(SERVICE_UUID)
                messageCharacteristic = service.getCharacteristic(SEND_MESSAGE_UUID)
                bluetoothGatt?.setCharacteristicNotification(messageCharacteristic, true)
//                val sendDescriptor =
//                    messageCharacteristic?.getDescriptor(UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"))
//                sendDescriptor?.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
//                bluetoothGatt?.writeDescriptor(sendDescriptor)

                responseMessageCharacteristic = service.getCharacteristic(RESPONSE_MESSAGE_UUID)
                bluetoothGatt?.setCharacteristicNotification(responseMessageCharacteristic, true)
//                val responseDescriptor =
//                    responseMessageCharacteristic?.getDescriptor(UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"))
//                responseDescriptor?.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
//                bluetoothGatt?.writeDescriptor(responseDescriptor)

            }
        }

        override fun onCharacteristicWrite(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?,
            status: Int
        ) {
            super.onCharacteristicWrite(gatt, characteristic, status)
            Log.d("devLog", "onCharacteristicWrite client 0: $status ${characteristic?.uuid}")
            val data = characteristic?.value
            Log.d("devLog", "onCharacteristicWrite client 0: $status ${data?.toHexString()}")
        }

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            newValue: ByteArray,
        ) {
            super.onCharacteristicChanged(gatt, characteristic, newValue)
            val message = newValue.toString(Charsets.UTF_8)
            Log.d("devLog", "onCharacteristicChanged client 1: ${message}")
        }

        override fun onCharacteristicChanged(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?) {
            super.onCharacteristicChanged(gatt, characteristic)
            Log.d("devLog", "onCharacteristicChanged client 2: ${characteristic?.uuid}")
            val data = characteristic?.value
            Log.d("devLog", "onCharacteristicChanged client 2 2: ${data?.toHexString()}")
        }
    }

    init {
        viewModelScope.launch {
            bleUseCase.getBleDeviceUseCase.invoke(GetBleDeviceUseCase.Params)
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), currentState.scannedDevices)
                .collect {
                    if (it.isNotEmpty()) {
                        setState { copy(scannedDevices = it) }
                        val stages = it.find { device -> device.address == "FF:F4:58:89:79:80" }
                        if (stages != null) {
                            call(bleUseCase.stopScanBleUseCase.invoke(StopScanBleUseCase.Params))
                        }
                    }
                }
        }
    }

    override fun createInitialConText(): Context = appContext

    override fun createInitialState(): ScanViewState = ScanViewState()

    @SuppressLint("MissingPermission")
    override fun onTriggerEvent(event: ScanViewEvent) {
        viewModelScope.launch {
            when (event) {
                is ScanViewEvent.ScanBle -> {
                    scanBle()
                }

                is ScanViewEvent.StopScanBle -> {
                    call(bleUseCase.stopScanBleUseCase.invoke(StopScanBleUseCase.Params))
                }

                is ScanViewEvent.ConnectToDevice -> {
                    Log.d("devLog", "onTriggerEvent: connect to device ${event.device.name} ${event.device.address}")
                    connectToDevice(event.device)
                    //call(bleUseCase.connectToDeviceUseCase.invoke(ConnectToDeviceUseCase.Params(address = event.device.address)))
                }

                is ScanViewEvent.Disconnect -> {
                    disconnect()
//                    call(bleUseCase.disconnectUSeCase.invoke(DisconnectUseCase.Params))
                }

                is ScanViewEvent.GetDataInfo -> {
//                    call(bleUseCase.writeCharacteristicUseCase.invoke(WriteCharacteristicUseCase.Params("5BB506000016")))
//                    bluetoothGatt?.readCharacteristic(responseMessageCharacteristic)
                    writeCharacteristic(messageWithCheckSum("5BB5040000"))
                }
            }
        }
    }

    private suspend fun scanBle() {
        call(bleUseCase.scanBleUseCase.invoke(ScanBleUseCase.Params))
    }

    @SuppressLint("MissingPermission")
    private fun connectToDevice(device: BluetoothDevice) {
        bluetoothGatt = device.connectGatt(appContext, false, bluetoothGattCallback)
    }

    @SuppressLint("MissingPermission")
    private fun disconnect() {
        bluetoothGatt?.disconnect()
        bluetoothGatt?.close()
    }

    @Suppress("DEPRECATION")
    @SuppressLint("MissingPermission")
    fun writeCharacteristic(message: String) {
        Log.d("devLog", "writeCharacteristic: $message $bluetoothGatt")
        messageCharacteristic?.let { characteristic ->
            characteristic.writeType = BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
            val messageBytes = message.hexStringToByteArray()
            bluetoothGatt?.let {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    val status = bluetoothGatt!!.writeCharacteristic(
                        characteristic,
                        messageBytes,
                        BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
                    )
                    if (status == BluetoothStatusCodes.SUCCESS) {
                        Log.d("devLog", "writeCharacteristic: success")
                    } else {
                        Log.d("devLog", "writeCharacteristic: fail")
                    }
                } else {
                    characteristic.value = messageBytes
                    val success = bluetoothGatt?.writeCharacteristic(messageCharacteristic)

                    if (success == true) {
                        Log.d("devLog", "writeCharacteristic: success")
                    } else {
                        Log.d("devLog", "writeCharacteristic: fail")
                    }
                }
            } ?: run {
                Log.d("devLog", "writeCharacteristic: bluetoothGatt is null")
            }
        } ?: run {
            Log.d("devLog", "writeCharacteristic: messageCharacteristic is null")
        }
    }

    private fun ByteArray.toHexString(): String = joinToString(separator = " ") { byte -> "%02x".format(byte) }
    private fun String.hexStringToByteArray(): ByteArray = chunked(2).map { it.toInt(16).toByte() }.toByteArray()

    private fun messageWithCheckSum(message: String): String {
        val checksum = calculateChecksum(message)
        val checksumHex = checksum.toString(16).padStart(2, '0')
        return message + checksumHex
    }

    private fun calculateChecksum(hex: String): Int {
        val byteArray = hex.chunked(2).map { it.toInt(16) }
        val checksum = byteArray.sum() and 0xFF
        return checksum
    }
}

sealed class ScanViewEvent : IViewEvent {
    data object ScanBle : ScanViewEvent()
    data object StopScanBle : ScanViewEvent()
    data class ConnectToDevice(val device: BluetoothDevice) : ScanViewEvent()
    data object Disconnect : ScanViewEvent()
    data object GetDataInfo : ScanViewEvent()
}