package com.colors.testble.domain.usecase

import com.colors.testble.domain.base.BaseUseCase
import com.colors.testble.domain.base.IParams
import com.colors.testble.domain.repository.BleRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class DisconnectUseCase(
    private val bleRepository: BleRepository
) : BaseUseCase<DisconnectUseCase.Params, Unit> {
    data object Params : IParams

    override suspend fun invoke(param: Params): Flow<Unit> = flow {
        emit(bleRepository.disconnect())
    }
}