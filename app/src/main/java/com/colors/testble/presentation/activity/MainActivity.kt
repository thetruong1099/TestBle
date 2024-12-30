package com.colors.testble.presentation.activity

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.content.ContextCompat
import androidx.navigation.compose.rememberNavController
import com.colors.testble.presentation.navigation_graph.RootNavGraph
import com.colors.testble.presentation.service.BLEService
import com.colors.testble.presentation.ui.theme.TestBleTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //startBLEForegroundService()
        enableEdgeToEdge()
        setContent {
            TestBleTheme {
                RootNavGraph(navController = rememberNavController())
            }
        }
    }

    private fun startBLEForegroundService() {
        val serviceIntent = Intent(this, BLEService::class.java)
        ContextCompat.startForegroundService(this, serviceIntent)
    }
}
