package com.colors.testble.presentation.screen.scanscreen

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@SuppressLint("MissingPermission")
@Composable
fun ScanScreen(
    viewState: ScanViewState,
    onEvent: (ScanViewEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .safeDrawingPadding()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(
                    onClick = {
                        onEvent.invoke(ScanViewEvent.ScanBle)
                    },
                ) {
                    Text(text = "Scan BLE")
                }

                Button(
                    onClick = {
                        onEvent.invoke(ScanViewEvent.StopScanBle)
                    },
                ) {
                    Text(text = "Stop Scan BLE")
                }

                Button(
                    onClick = {
                        onEvent.invoke(ScanViewEvent.Disconnect)
                    },
                ) {
                    Text(text = "Disconnect BLE")
                }
            }

            LazyRow (
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                item {
                    Button(
                        onClick = {
                            onEvent.invoke(ScanViewEvent.GetDataInfo)
                        },
                    ) {
                        Text(text = "Get Data Info BLE")
                    }
                }
            }


            LazyColumn(
                modifier = Modifier
                    .fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(
                    count = viewState.scannedDevices.count(),
                    key = { viewState.scannedDevices[it].address }) { index ->
                    val device = viewState.scannedDevices[index]
                    Card(
                        onClick = {
                            onEvent.invoke(ScanViewEvent.ConnectToDevice(device))
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(text = "Device Name: ${device.name ?: "Unknown"}")
                            Text(text = "Device Address: ${device.address}")
                        }
                    }
                }
            }

        }
    }
}