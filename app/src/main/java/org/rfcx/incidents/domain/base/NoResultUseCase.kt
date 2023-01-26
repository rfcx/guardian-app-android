package org.rfcx.incidents.domain.base

import kotlinx.coroutines.flow.Flow

abstract class NoResultUseCase {
    /**
     * Triggers the execution of this use case
     */
    suspend fun launch() {
        performAction()
    }
    protected abstract fun performAction()
}
