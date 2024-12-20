package com.colors.testble.presentation.screen.mainscreen

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat

@Composable
fun MainScreen(
    onEvent: (MainViewEvent) -> Unit,
    onNavigateToLogScreen: () -> Unit,
) {
    val permissionsForAddStage = when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> {
            listOf(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.BLUETOOTH_ADVERTISE,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.POST_NOTIFICATIONS,
            )
        }

        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU -> {
            listOf(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
            )
        }

        else -> {
            listOf(
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
            )
        }
    }

    val context = LocalContext.current
    var permissionGrand by remember { mutableStateOf(false) }

    fun checkPermissions(
        context: Context,
        permissions: List<String>,
    ): Boolean =
        permissions.all {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }

    val launcher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestMultiplePermissions(),
        ) { permissionsResult ->
            if (permissionsResult.all { it.value }) {
                permissionGrand = checkPermissions(context, permissionsForAddStage)
            }
        }

    LaunchedEffect(Unit) {
        val missingPermissions =
            permissionsForAddStage.filter {
                ContextCompat.checkSelfPermission(context, it) != PackageManager.PERMISSION_GRANTED
            }

        if (missingPermissions.isNotEmpty()) {
            launcher.launch(missingPermissions.toTypedArray())
        } else {
            permissionGrand = checkPermissions(context, permissionsForAddStage)
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(top = 16.dp)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Button(
                onClick = {
                    onEvent.invoke(MainViewEvent.ScanBle)
                },
                enabled = permissionGrand
            ) {
                Text(text = "Scan BLE Devices")
            }

            Button(
                onClick = {
                    onEvent.invoke(MainViewEvent.ConnectToDevice1)
                },
                enabled = permissionGrand
            ) {
                Text(text = "Connect BLE Device1")
            }

            Button(
                onClick = {
                    onEvent.invoke(MainViewEvent.ConnectToDevice2)
                },
                enabled = permissionGrand
            ) {
                Text(text = "Connect BLE Device2")
            }

            Button(
                onClick = {
                    onEvent.invoke(MainViewEvent.Disconnect)
                },
                enabled = permissionGrand
            ) {
                Text(text = "Disconnect BLE Devices")
            }

            Button(
                onClick = {
                    onEvent.invoke(MainViewEvent.SendMessage)
                },
                enabled = permissionGrand
            ) {
                Text(text = "Send BLE Devices")
            }

            Button(
                onClick = {
                    onNavigateToLogScreen.invoke()
                },
                enabled = permissionGrand
            ) {
                Text(text = "Navigate to Log Screen")
            }
        }
    }
}