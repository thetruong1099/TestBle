package com.colors.testble.domain.base

import kotlinx.coroutines.flow.Flow

interface IParams

interface BaseUseCase<T : IParams, R : Any> {
    suspend operator fun invoke(param: T): Flow<R>
}
