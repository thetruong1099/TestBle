package com.colors.testble.presentation.screen.mainscreen

import android.content.Context
import androidx.lifecycle.viewModelScope
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.colors.testble.data.worker.SendMessageWorker
import com.colors.testble.domain.usecase.BleUseCase
import com.colors.testble.domain.usecase.ConnectToDeviceUseCase
import com.colors.testble.domain.usecase.DisconnectUseCase
import com.colors.testble.domain.usecase.ScanBleUseCase
import com.colors.testble.domain.usecase.WriteCharacteristicUseCase
import com.colors.testble.presentation.base.BaseViewModel
import com.colors.testble.presentation.base.IViewEvent
import com.colors.testble.presentation.utils.getCurrentFormattedTime
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class MainViewModel
@Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val bleUseCase: BleUseCase,
) : BaseViewModel<MainViewState, MainViewEvent>() {

    init {
//        viewModelScope.launch {
//            call(bleUseCase.startGattServerUseCase.invoke(StartGattServerUseCase.Params))
//            bleUseCase.getBleDeviceUseCase.invoke(GetBleDeviceUseCase.Params)
//                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), currentState.scannedDevices)
//                .collect {
//                    Log.d("devLog", "scanBle: $it")
//                    if (it.isNotEmpty()) {
//                        setState { copy(scannedDevices = it) }
//                    }
//                }
//        }
    }

    override fun createInitialConText(): Context = appContext

    override fun createInitialState(): MainViewState = MainViewState

    override fun onTriggerEvent(event: MainViewEvent) {
        viewModelScope.launch {
            when (event) {
                MainViewEvent.ScanBle -> {
                    call(bleUseCase.scanBleUseCase.invoke(ScanBleUseCase.Params))
                }

                MainViewEvent.ConnectToDevice1 -> {
                    call(bleUseCase.connectToDeviceUseCase.invoke(ConnectToDeviceUseCase.Params("B8:94:E7:3D:46:0E")))
                }

                MainViewEvent.ConnectToDevice2 -> {
                    call(bleUseCase.connectToDeviceUseCase.invoke(ConnectToDeviceUseCase.Params("20:34:FB:A6:55:A6")))
                }

                MainViewEvent.Disconnect -> {
                    call(bleUseCase.disconnectUSeCase.invoke(DisconnectUseCase.Params))
                }

                MainViewEvent.SendMessage -> {
                    send()
                    //sendMessage()
                    //call(bleUseCase.writeCharacteristicUseCase.invoke(WriteCharacteristicUseCase.Params("Hello")))
                }
            }
        }
    }

    private suspend fun sendMessage() {
        val workRequest = PeriodicWorkRequestBuilder<SendMessageWorker>(5, TimeUnit.MINUTES).build()

        WorkManager.getInstance(appContext).enqueue(workRequest)
    }

    private suspend fun send() {
        call(bleUseCase.writeCharacteristicUseCase.invoke(WriteCharacteristicUseCase.Params("Hello ${getCurrentFormattedTime()}")))
        delay(60000)
        send()
    }
}

sealed class MainViewEvent : IViewEvent {
    data object ScanBle : MainViewEvent()
    data object ConnectToDevice1 : MainViewEvent()
    data object ConnectToDevice2 : MainViewEvent()
    data object Disconnect : MainViewEvent()
    data object SendMessage : MainViewEvent()
}