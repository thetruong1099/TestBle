package com.colors.testble.domain.usecase

import android.bluetooth.BluetoothDevice
import com.colors.testble.domain.base.BaseUseCase
import com.colors.testble.domain.base.IParams
import com.colors.testble.domain.repository.BleRepository
import kotlinx.coroutines.flow.Flow

class GetBleDeviceUseCase(
    private val bleRepository: BleRepository,
) : BaseUseCase<GetBleDeviceUseCase.Params, List<BluetoothDevice>> {
    data object Params : IParams

    override suspend fun invoke(param: Params): Flow<List<BluetoothDevice>> = bleRepository.getScannedDeviceList()
}
