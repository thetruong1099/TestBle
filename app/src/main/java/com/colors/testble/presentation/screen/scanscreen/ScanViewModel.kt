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
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
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
    private var historyDataNumbered: String = ""

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

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            value: ByteArray
        ) {
            super.onCharacteristicChanged(gatt, characteristic, value)
            Log.d("devLog", "onCharacteristicChanged client 1: ${value.toHexString()}")
        }

        @Suppress("DEPRECATION")
        @Deprecated("Deprecated in Java")
        override fun onCharacteristicChanged(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?) {
            super.onCharacteristicChanged(gatt, characteristic)
            val data = characteristic?.value
            val dataListString = data?.toHexListString()
            if ((dataListString?.size ?: 0) > 3) {
                getDataStage(dataListString!!)
            } else {
                Log.d("devLog", "onCharacteristicChanged client 2 4: not message of stage${dataListString?.toHeader()}")
            }
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
                    sendRequestGetBasicInfo()
                }

                is ScanViewEvent.GetBatteryPower -> {
                    sendRequestGetBatteryPower()
                }

                is ScanViewEvent.SetCurrentTime -> {
                    sendRequestSetCurrentTime()
                }

                is ScanViewEvent.StartWateringMode -> {
                    sendRequestStartWateringMode()
                }

                is ScanViewEvent.ExitWateringMode -> {
                    sendRequestExitWateringMode()
                }

                is ScanViewEvent.StartCalibrationMode -> {
                    sendRequestStartCalibrationMode()
                }

                is ScanViewEvent.ExitCalibrationMode -> {
                    sendRequestExitCalibrationMode()
                }

                is ScanViewEvent.ResetCommand -> {
                    sendResetCommand()
                }

                is ScanViewEvent.SendRequestOfflineData -> {
                    sendRequestOfflineData()
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
        Log.d("devLog", "writeCharacteristic: $message")
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

    private fun ByteArray.toHexString(): String =
        joinToString(separator = " ") { byte -> "%02x".format(byte) }

    private fun ByteArray.toHexListString(): List<String> =
        joinToString(separator = " ") { byte -> "%02x".format(byte) }.split(" ")

    private fun List<String>.toHeader(): String = this[0] + this[1] + this[2]

    private fun String.hexStringToByteArray(): ByteArray = chunked(2).map { it.toInt(16).toByte() }.toByteArray()

    private fun String.toHexString(): String =
        this.toByteArray().joinToString(separator = "") { byte -> "%02x".format(byte) }

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


    private fun Long.unixTimestampToFormattedTime(): String {
        val date = Date(this * 1000)
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        return dateFormat.format(date)
    }

    private fun getDataStage(data: List<String>) {
        val header = data.toHeader()
        when (header.uppercase()) {
            BleMessageResponseCommand.REQUEST_OFFLINE_DATA.value -> {
                Log.d("data", "getDataStage: ${data.joinToString("")}")
                historyDataNumbered = ""
            }

            BleMessageResponseCommand.OFFLINE_DATA.value -> {
                val dataNumbered = data.slice(5..6).joinToString("")
                val timestamp = data.slice(7..10).joinToString("").toLong(16)
                val time = timestamp.unixTimestampToFormattedTime()
                val weight = data.slice(11..12).joinToString("").toInt(16)
                val temperature = data.slice(13..14).joinToString("").toInt(16)
                Log.d(
                    "devLog",
                    "getDataStage OFFLINE_DATA: $historyDataNumbered $dataNumbered $time $weight $temperature"
                )
                if (historyDataNumbered != dataNumbered) {
                    sendConfirmOfflineData(dataNumbered)
                    historyDataNumbered = dataNumbered
                }
            }

            BleMessageResponseCommand.RESET_COMMAND.value -> {
                Log.d("devLog", "getDataStage RESET_COMMAND: ${data.joinToString("")}")
            }

            BleMessageResponseCommand.QUERY_POWER.value -> {}
            BleMessageResponseCommand.SEND_CURRENT_TIMESTAMP.value -> {
                Log.d("devLog", "getDataStage SEND_CURRENT_TIMESTAMP: ${data.joinToString("")}")
            }

            BleMessageResponseCommand.BASIC_INFORMATION.value -> {
                Log.d("devLog", "getDataStage: $data")
                val deviceMode = data[5].toInt(16)
                Log.d("devLog", "getDataStage BASIC_INFORMATION: $deviceMode")
            }

            BleMessageResponseCommand.DEVICE_FAILURE_STATUS_CODE.value -> {
                Log.d("devLog", "getDataStage DEVICE_FAILURE_STATUS_CODE: ${data.joinToString("")}")
            }

            BleMessageResponseCommand.MEASURING_STATUS_CODE.value -> {
                val deviceMode = data[5]
                val timestamp = data.slice(6..9).joinToString("").toLong(16)
                val time = timestamp.unixTimestampToFormattedTime()
                val weight = data.slice(10..11).joinToString("").toInt(16)
                val temperature = data.slice(12..13).joinToString("").toInt(16)
                val power = data[14].toInt(16)
                Log.d(
                    "devLog",
                    "getDataStage MEASURING_STATUS_CODE: $deviceMode $timestamp $time $weight $temperature $power"
                )
            }

            BleMessageResponseCommand.WATERING_MODE.value -> {
                val deviceMode = data[5]
                val weight = data.slice(6..7).joinToString("").toInt(16)
                val temperature = data.slice(8..9).joinToString("").toInt(16)
                val power = data[10].toInt(16)

                Log.d("devLog", "getDataStage WATERING_MODE: $deviceMode $weight $temperature $power")

            }

            BleMessageResponseCommand.RESPONSE_WATERING_MODE.value -> {
                val status = data[5]
                Log.d("devLog", "getDataStage RESPONSE_WATERING_MODE: ${data.joinToString(" ")} $status")
            }

            BleMessageResponseCommand.RESPONSE_CALIBRATION_MODE.value -> {
                val status = data[5]
                Log.d("devLog", "getDataStage RESPONSE_CALIBRATION_MODE: ${data.joinToString(" ")} $status")
            }

            else -> {
                Log.d("devLog", "getDataStage else: ${data.joinToString("")}")
            }
        }
    }

    private fun sendRequestGetBasicInfo() {
        writeCharacteristic(messageWithCheckSum("5BB5060000"))
    }

    private fun sendRequestGetBatteryPower() {
        writeCharacteristic(messageWithCheckSum("5BB5040000"))
    }

    private fun sendRequestSetCurrentTime() {
        val currentTime = Calendar.getInstance(TimeZone.getTimeZone("UTC")).timeInMillis / 1000
        val currentTimeHex = currentTime.toString(16)
        val message = BleRequestCommand.SEND_CURRENT_TIMESTAMP.generateRequestCommand(currentTimeHex)
        Log.d("devLog", "sendRequestSetCurrentTime: $currentTime $currentTimeHex $message")
        writeCharacteristic(message)
    }

    private fun sendRequestStartWateringMode() {
        writeCharacteristic(BleRequestCommand.SEND_START_WATERING_MODE.generateRequestCommand(""))
    }

    private fun sendRequestExitWateringMode() {
        writeCharacteristic(BleRequestCommand.SEND_EXIT_WATERING_MODE.generateRequestCommand(""))
    }

    private fun sendRequestStartCalibrationMode() {
        writeCharacteristic(BleRequestCommand.SEND_START_CALIBRATION_MODE.generateRequestCommand(""))
    }

    private fun sendRequestExitCalibrationMode() {
        writeCharacteristic(BleRequestCommand.SEND_EXIT_CALIBRATION_MODE.generateRequestCommand(""))
    }

    private fun sendResetCommand() {
        writeCharacteristic(BleRequestCommand.RESET_COMMAND.generateRequestCommand(""))
    }

    private fun sendRequestOfflineData() {
        writeCharacteristic(BleRequestCommand.REQUEST_OFFLINE_DATA.generateRequestCommand(""))
    }

    private fun sendConfirmOfflineData(message: String) {
        writeCharacteristic(BleRequestCommand.CONFIRM_OFFLINE_DATA.generateRequestCommand(message))
    }
}

sealed class ScanViewEvent : IViewEvent {
    data object ScanBle : ScanViewEvent()
    data object StopScanBle : ScanViewEvent()
    data class ConnectToDevice(val device: BluetoothDevice) : ScanViewEvent()
    data object Disconnect : ScanViewEvent()
    data object GetDataInfo : ScanViewEvent()
    data object GetBatteryPower : ScanViewEvent()
    data object SetCurrentTime : ScanViewEvent()
    data object StartWateringMode : ScanViewEvent()
    data object ExitWateringMode : ScanViewEvent()
    data object StartCalibrationMode : ScanViewEvent()
    data object ExitCalibrationMode : ScanViewEvent()
    data object ResetCommand : ScanViewEvent()
    data object SendRequestOfflineData : ScanViewEvent()
}

enum class BleRequestCommand {
    REQUEST_OFFLINE_DATA,
    CONFIRM_OFFLINE_DATA,
    RESET_COMMAND,
    QUERY_POWER,
    SEND_CURRENT_TIMESTAMP,
    READ_BASIC_INFORMATION,
    DEVICE_FAILURE_STATUS_CODE,
    MEASURING_STATUS_CODE,
    OFFLINE_DATA,
    SEND_START_CALIBRATION_MODE,
    SEND_EXIT_CALIBRATION_MODE,
    SEND_START_WATERING_MODE,
    SEND_EXIT_WATERING_MODE;

    fun generateRequestCommand(message: String): String {
        return when (this) {
            REQUEST_OFFLINE_DATA -> {
                val messageWithHeader = "5BB5010000"
                val checkSum = calculateChecksum(messageWithHeader)
                return "$messageWithHeader${checkSum}"
            }

            CONFIRM_OFFLINE_DATA -> {
                val messageWithHeader = "5BB5020002$message"
                val checkSum = calculateChecksum(messageWithHeader)
                return "$messageWithHeader${checkSum}"
            }

            RESET_COMMAND -> {
                val messageWithHeader = "5BB5030000"
                val checkSum = calculateChecksum(messageWithHeader)
                return "$messageWithHeader${checkSum}"
            }

            QUERY_POWER -> "5BB5040000"
            SEND_CURRENT_TIMESTAMP -> {
                val messageWithHeader = "5BB5050004$message"
                val checkSum = calculateChecksum(messageWithHeader)
                return "$messageWithHeader${checkSum}"
            }

            READ_BASIC_INFORMATION -> "5BB5060000"
            DEVICE_FAILURE_STATUS_CODE -> "5BB5810000"
            MEASURING_STATUS_CODE -> "5BB5820000"
            OFFLINE_DATA -> "5BB5830000"
            SEND_START_CALIBRATION_MODE -> {
                val messageWithHeader = "5BB507000101"
                val checkSum = calculateChecksum(messageWithHeader)
                return "$messageWithHeader${checkSum}"
            }

            SEND_EXIT_CALIBRATION_MODE -> {
                val messageWithHeader = "5BB507000100"
                val checkSum = calculateChecksum(messageWithHeader)
                return "$messageWithHeader${checkSum}"
            }

            SEND_START_WATERING_MODE -> {
                val messageWithHeader = "5BB508000101"
                val checkSum = calculateChecksum(messageWithHeader)
                return "$messageWithHeader${checkSum}"
            }

            SEND_EXIT_WATERING_MODE -> {
                val messageWithHeader = "5BB508000100"
                val checkSum = calculateChecksum(messageWithHeader)
                return "$messageWithHeader${checkSum}"
            }
        }
    }

    private fun calculateChecksum(hex: String): String {
        val byteArray = hex.chunked(2).map { it.toInt(16) }
        val checksum = byteArray.sum() and 0xFF
        return checksum.toString(16).padStart(2, '0')
    }
}

enum class BleMessageResponseCommand(val value: String) {
    REQUEST_OFFLINE_DATA("5BB501"),
    OFFLINE_DATA("5BB583"),
    RESET_COMMAND("5BB503"),
    QUERY_POWER("5BB504"),
    SEND_CURRENT_TIMESTAMP("5BB505"),
    BASIC_INFORMATION("5BB585"),
    DEVICE_FAILURE_STATUS_CODE("5BB581"),
    MEASURING_STATUS_CODE("5BB582"),
    WATERING_MODE("5BB584"),
    RESPONSE_WATERING_MODE("5BB508"),
    RESPONSE_CALIBRATION_MODE("5BB507")
}