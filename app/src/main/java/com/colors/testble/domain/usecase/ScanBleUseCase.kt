package com.colors.testble.domain.usecase

import com.colors.testble.domain.base.BaseUseCase
import com.colors.testble.domain.base.IParams
import com.colors.testble.domain.repository.BleRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class ScanBleUseCase(
    private val bleRepository: BleRepository,
) : BaseUseCase<ScanBleUseCase.Params, Unit> {
    data object Params : IParams

    override suspend fun invoke(param: Params): Flow<Unit> =
        flow {
            emit(bleRepository.startScanning())
//            delay(10000)
//            emit(bleRepository.stopScanning())
        }
}
