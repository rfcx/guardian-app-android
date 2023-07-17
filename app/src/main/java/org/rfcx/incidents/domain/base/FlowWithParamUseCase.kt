package org.rfcx.incidents.domain.base

import kotlinx.coroutines.flow.Flow

abstract class FlowWithParamUseCase<in Params, T> {
    /**
     * Triggers the execution of this use case
     */
    suspend fun launch(param: Params): Flow<T> {
        return performAction(param)
    }

    protected abstract fun performAction(param: Params): Flow<T>
}
