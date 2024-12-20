package com.colors.testble.presentation.screen.logscreen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun LogScreen(
    viewState: LogViewState,
    onEvent: (LogViewEvent) -> Unit,
    modifier: Modifier = Modifier
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
            Button(
                onClick = {
                    onEvent.invoke(LogViewEvent.GetLogList)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Get log")
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(
                    count = viewState.logList.count(),
                    key = { viewState.logList[it].id }) { index ->
                    val log = viewState.logList[index]
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(text = log.message)
                            Text(text = log.time)
                        }
                    }

                }
            }
        }

    }
}