package com.colors.testble.presentation.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BLEService : Service() {
    override fun onBind(intent: Intent?): IBinder? = null
}