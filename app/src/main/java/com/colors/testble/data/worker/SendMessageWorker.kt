package com.colors.testble.data.worker

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.colors.testble.data.bluetooth.server.BLEServer
import com.colors.testble.presentation.utils.getCurrentFormattedTime

class SendMessageWorker(
    appContext: Context, workerParams: WorkerParameters
) : Worker(appContext, workerParams) {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun doWork(): Result = try {
        // Send the message here
        Log.d("devLog", "doWork: ")
        BLEServer.writeCharacteristic("Hello ${getCurrentFormattedTime()}")
        Result.success()
    } catch (e: Exception) {
        Result.failure()
    }
}