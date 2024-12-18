package com.colors.testble.domain.usecase

import com.colors.testble.domain.base.BaseUseCase
import com.colors.testble.domain.base.IParams
import com.colors.testble.domain.repository.BleRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class WriteCharacteristicUseCase(
    private val bleRepository: BleRepository
) : BaseUseCase<WriteCharacteristicUseCase.Params, Unit> {
    data class Params(
        val string: String
    ) : IParams

    override suspend fun invoke(param: Params): Flow<Unit> = flow {
        emit(bleRepository.writeCharacteristic(param.string))
    }
}