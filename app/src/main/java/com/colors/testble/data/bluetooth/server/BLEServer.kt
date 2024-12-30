package com.colors.testble.data.bluetooth.server

import android.annotation.SuppressLint
import android.app.Application
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothGattServer
import android.bluetooth.BluetoothGattServerCallback
import android.bluetooth.BluetoothGattService
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.bluetooth.BluetoothStatusCodes
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.bluetooth.le.BluetoothLeAdvertiser
import android.os.Build
import android.os.ParcelUuid
import android.util.Log
import com.colors.testble.data.local.datasource.RealmDataSource
import com.colors.testble.data.local.entity.LogEntity
import com.colors.testble.data.utils.RESPONSE_MESSAGE_UUID
import com.colors.testble.data.utils.SEND_MESSAGE_UUID
import com.colors.testble.data.utils.SERVICE_UUID
import io.realm.kotlin.Realm
import java.util.UUID

object BLEServer {
    private var appContext: Application? = null
    private var bleDatabase: Realm? = null
    private var bluetoothAdapter: BluetoothAdapter? = null
    private var bluetoothManager: BluetoothManager? = null

    private var gattServer: BluetoothGattServer? = null
    private var gattServerCallback: BluetoothGattServerCallback? = null

    private var gattClient: BluetoothGatt? = null
    private var gattClientCallback: BluetoothGattCallback? = null

    // This property will be null if bluetooth is not enabled or if advertising is not
    // possible on the device
    private var advertiser: BluetoothLeAdvertiser? = null
    private var advertiseCallback: AdvertiseCallback? = null
    private var advertiseSettings: AdvertiseSettings = buildAdvertiseSettings()
    private var advertiseData: AdvertiseData = buildAdvertiseData()

    private var gatt: BluetoothGatt? = null
    private var messageCharacteristic: BluetoothGattCharacteristic? = null

    fun startServer(
        app: Application,
        bleAdapter: BluetoothAdapter,
        bleManager: BluetoothManager,
        database: Realm
    ) {
        appContext = app
        bleDatabase = database
        bluetoothAdapter = bleAdapter
        bluetoothManager = bleManager
        setupGattServer()
        startAdvertisement()
    }

    fun stopServer() {
        stopAdvertising()
    }

    @SuppressLint("MissingPermission")
    fun connect(address: String) {
        gattClientCallback = GattClientCallback()
        gattClient = bluetoothAdapter!!.getRemoteDevice(address).connectGatt(null, false, gattClientCallback)
    }

    @SuppressLint("MissingPermission")
    fun disconnect() {
        gattClient?.disconnect()
        gattClient?.close()
    }

    @Suppress("DEPRECATION")
    @SuppressLint("MissingPermission")
    fun writeCharacteristic(message: String) {
        Log.d("devLog", "writeCharacteristic: $message $gatt")
        messageCharacteristic?.let { characteristic ->
            characteristic.writeType = BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
            val messageBytes = message.toByteArray(Charsets.UTF_8)
            gatt?.let {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    val status = gatt!!.writeCharacteristic(
                        characteristic,
                        messageBytes,
                        BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
                    )
                    insertLog("writeCharacteristic: ${status == BluetoothStatusCodes.SUCCESS}")
                    if (status == BluetoothStatusCodes.SUCCESS) {
                        insertLog("writeCharacteristic: success")
                    } else {
                        insertLog("writeCharacteristic: failed")
                    }
                } else {
                    characteristic.value = messageBytes
                    val success = gatt?.writeCharacteristic(messageCharacteristic)
                    insertLog("writeCharacteristic: $success")
                }
            } ?: run {
                insertLog("sendMessage: no gatt connection to send a message with")
            }
        } ?: run {
            insertLog("sendMessage: no message characteristic to send a message with")
        }
    }

    /**
     * Function to setup a local GATT server.
     * This requires setting up the available services and characteristics that other devices
     * can read and modify.
     */
    @SuppressLint("MissingPermission")
    private fun setupGattServer() {
        gattServerCallback = GattServerCallback()

        gattServer = bluetoothManager!!.openGattServer(
            appContext!!,
            gattServerCallback!!
        ).apply {
            addService(setupGattService())
        }
    }

    /**
     * Function to create the GATT Server with the required characteristics and descriptors
     */
    private fun setupGattService(): BluetoothGattService {
        // Setup gatt service
        val service = BluetoothGattService(SERVICE_UUID, BluetoothGattService.SERVICE_TYPE_PRIMARY)
        // need to ensure that the property is writable and has the write permission
        val messageCharacteristic = BluetoothGattCharacteristic(
            SEND_MESSAGE_UUID,
            BluetoothGattCharacteristic.PROPERTY_WRITE,
            BluetoothGattCharacteristic.PERMISSION_WRITE
        )
        service.addCharacteristic(messageCharacteristic)
        val confirmCharacteristic = BluetoothGattCharacteristic(
            RESPONSE_MESSAGE_UUID,
            BluetoothGattCharacteristic.PROPERTY_WRITE,
            BluetoothGattCharacteristic.PERMISSION_WRITE
        )
        service.addCharacteristic(confirmCharacteristic)

        return service
    }

    /**
     * Start advertising this device so other BLE devices can see it and connect
     */
    @SuppressLint("MissingPermission")
    private fun startAdvertisement() {
        advertiser = bluetoothAdapter!!.bluetoothLeAdvertiser
        insertLog("startAdvertisement: with advertiser $advertiser")

        if (advertiseCallback == null) {
            advertiseCallback = DeviceAdvertiseCallback()

            advertiser?.startAdvertising(advertiseSettings, advertiseData, advertiseCallback)
        }
    }

    /**
     * Stops BLE Advertising.
     */
    @SuppressLint("MissingPermission")
    private fun stopAdvertising() {
        insertLog("Stopping Advertising with advertiser $advertiser")
        advertiser?.stopAdvertising(advertiseCallback)
        advertiseCallback = null
    }

    /**
     * Returns an AdvertiseSettings object set to use low power (to help preserve battery life)
     * and disable the built-in timeout since this code uses its own timeout runnable.
     */
    private fun buildAdvertiseSettings(): AdvertiseSettings {
        return AdvertiseSettings.Builder()
            .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_POWER)
            .setTimeout(0)
            .build()
    }

    /**
     * Returns an AdvertiseData object which includes the Service UUID and Device Name.
     */
    private fun buildAdvertiseData(): AdvertiseData {
        /**
         * Note: There is a strict limit of 31 Bytes on packets sent over BLE Advertisements.
         * This limit is outlined in section 2.3.1.1 of this document:
         * https://inst.eecs.berkeley.edu/~ee290c/sp18/note/BLE_Vol6.pdf
         *
         * This limit includes everything put into AdvertiseData including UUIDs, device info, &
         * arbitrary service or manufacturer data.
         * Attempting to send packets over this limit will result in a failure with error code
         * AdvertiseCallback.ADVERTISE_FAILED_DATA_TOO_LARGE. Catch this error in the
         * onStartFailure() method of an AdvertiseCallback implementation.
         */
        val dataBuilder = AdvertiseData.Builder()
            .addServiceUuid(ParcelUuid(SERVICE_UUID))
            .setIncludeDeviceName(true)
        return dataBuilder.build()
    }

    /**
     * Custom callback for the Gatt Server this device implements
     */
    private class GattServerCallback : BluetoothGattServerCallback() {
        override fun onConnectionStateChange(device: BluetoothDevice, status: Int, newState: Int) {
            super.onConnectionStateChange(device, status, newState)
            val isSuccess = status == BluetoothGatt.GATT_SUCCESS
            val isConnected = newState == BluetoothProfile.STATE_CONNECTED
            insertLog("onConnectionStateChange: Server success: $isSuccess connected: $isConnected device $device")
            if (isSuccess && isConnected) {
                //connect(device.address)
                insertLog("onConnectionStateChange: have device connected $device")
            } else {
                insertLog("onConnectionStateChange: have device disconnected $device")
            }
        }

        @SuppressLint("MissingPermission")
        override fun onCharacteristicWriteRequest(
            device: BluetoothDevice,
            requestId: Int,
            characteristic: BluetoothGattCharacteristic,
            preparedWrite: Boolean,
            responseNeeded: Boolean,
            offset: Int,
            value: ByteArray?
        ) {
            super.onCharacteristicWriteRequest(
                device,
                requestId,
                characteristic,
                preparedWrite,
                responseNeeded,
                offset,
                value
            )
            insertLog("onCharacteristicWriteRequest: ${characteristic.uuid}")
            if (characteristic.uuid == RESPONSE_MESSAGE_UUID) {
                gattServer?.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, 0, null)
                val message = value?.toString(Charsets.UTF_8)
                insertLog("onCharacteristicWriteRequest: Have message: $message")
            }
        }

        override fun onCharacteristicReadRequest(
            device: BluetoothDevice?,
            requestId: Int,
            offset: Int,
            characteristic: BluetoothGattCharacteristic?
        ) {
            super.onCharacteristicReadRequest(device, requestId, offset, characteristic)
            Log.d("devLog", "onCharacteristicReadRequest: ${characteristic?.uuid}")
        }
    }

    private class GattClientCallback : BluetoothGattCallback() {
        @SuppressLint("MissingPermission")
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            super.onConnectionStateChange(gatt, status, newState)
            val isSuccess = status == BluetoothGatt.GATT_SUCCESS
            val isConnected = newState == BluetoothProfile.STATE_CONNECTED
            insertLog("onConnectionStateChange: Client $gatt  success: $isSuccess connected: $isConnected")
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
                insertLog("onServicesDiscovered: Have gatt1 $discoveredGatt")
                gatt = discoveredGatt
                val service = discoveredGatt.getService(SERVICE_UUID)
                messageCharacteristic = service.getCharacteristic(SEND_MESSAGE_UUID)
                discoveredGatt.services.forEach { service ->
                    insertLog("onServicesDiscovered: Have gatt1 service uuid ${service.uuid}")
                    service.getCharacteristic(RESPONSE_MESSAGE_UUID)?.let { characteristic ->
                        insertLog("onServicesDiscovered: Have gatt1 character uuid ${characteristic.uuid}")
                    }
                    service.characteristics.forEach { characteristic ->
                        gatt?.setCharacteristicNotification(characteristic, true)
                        val descriptor =
                            characteristic?.getDescriptor(UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"))
                        descriptor?.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                        gatt?.writeDescriptor(descriptor)
                        insertLog("onServicesDiscovered: Have gatt1 character uuid ${characteristic.uuid}")
                    }
                }
//                gatt = discoveredGatt
//                insertLog("onServicesDiscovered: Have gatt2")
//                val service = discoveredGatt.getService(SERVICE_UUID)
//                insertLog("onServicesDiscovered: Have gatt3 ${service}")
//                messageCharacteristic = service.getCharacteristic(MESSAGE_UUID)
//                insertLog("onServicesDiscovered: Have gatt4 $messageCharacteristic")
            }
        }

        override fun onCharacteristicWrite(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?,
            status: Int
        ) {
            super.onCharacteristicWrite(gatt, characteristic, status)
            Log.d("devLog", "onCharacteristicWrite client 1: $status ${characteristic?.uuid}")
        }

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            newValue: ByteArray,
        ) {
            super.onCharacteristicChanged(gatt, characteristic, newValue)
            Log.d("devLog", "onCharacteristicChanged client 1: ${characteristic.uuid}")
        }

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?
        ) {
            super.onCharacteristicChanged(gatt, characteristic)
            Log.d("devLog", "onCharacteristicChanged client 2: ${characteristic?.uuid}")
        }

        override fun onCharacteristicRead(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            value: ByteArray,
            status: Int
        ) {
            super.onCharacteristicRead(gatt, characteristic, value, status)
            Log.d("devLog", "onCharacteristicRead client 3: $status ${characteristic.uuid}")
        }

        override fun onDescriptorWrite(
            gatt: BluetoothGatt,
            descriptor: BluetoothGattDescriptor,
            status: Int
        ) {
            super.onDescriptorWrite(gatt, descriptor, status)
            Log.d("devLog", "onDescriptorWrite client 4: $status ${descriptor.uuid}")
        }
    }

    /**
     * Custom callback after Advertising succeeds or fails to start. Broadcasts the error code
     * in an Intent to be picked up by AdvertiserFragment and stops this Service.
     */
    private class DeviceAdvertiseCallback : AdvertiseCallback() {
        override fun onStartFailure(errorCode: Int) {
            super.onStartFailure(errorCode)
            // Send error state to display
            insertLog("Advertise failed with error: $errorCode")
            //_viewState.value = DeviceScanViewState.Error(errorMessage)
        }

        override fun onStartSuccess(settingsInEffect: AdvertiseSettings) {
            super.onStartSuccess(settingsInEffect)
            insertLog("Advertising successfully started")
        }
    }

    private fun insertLog(messageLog: String) {
        bleDatabase?.let {
            RealmDataSource.insertLog(bleDatabase!!,
                LogEntity().apply {
                    message = messageLog
                }
            )
        } ?: run {
            Log.d("devLog", "onServicesDiscovered: $bleDatabase ")
        }
    }
}