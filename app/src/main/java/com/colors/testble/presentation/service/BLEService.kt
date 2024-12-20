package com.colors.testble.presentation.service

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.colors.testble.R
import com.colors.testble.domain.usecase.BleUseCase
import com.colors.testble.domain.usecase.StartGattServerUseCase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class BLEService : Service() {

    @Inject
    lateinit var bleUseCase: BleUseCase

    override fun onCreate() {
        super.onCreate()
        startForegroundService()
        startGattServer()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
    }

    @SuppressLint("ForegroundServiceType")
    private fun startForegroundService() {
        val channelId = "BLEForegroundServiceChannel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "BLE Foreground Service Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }

        val notification: Notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("BLE GATT Server")
            .setContentText("Running BLE GATT Server in foreground")
            .setSmallIcon(R.mipmap.ic_launcher)
            .build()

        startForeground(1, notification)
    }

    private fun startGattServer() {
        CoroutineScope(Dispatchers.Default).launch {
            bleUseCase.startGattServerUseCase.invoke(StartGattServerUseCase.Params).collect {}
        }
    }
}