package org.rfcx.incidents.domain.base

import kotlinx.coroutines.flow.Flow

abstract class FlowUseCase<T> {
    /**
     * Triggers the execution of this use case
     */
    suspend fun launch(): Flow<T> {
        return performAction()
    }

    protected abstract fun performAction(): Flow<T>
}
