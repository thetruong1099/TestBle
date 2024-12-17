package com.colors.testble.data.bluetooth.connection

interface BleConnectionManager {
    fun startServer()
    fun stopServer()
    fun connect(address: String)
    fun disconnect()
}
