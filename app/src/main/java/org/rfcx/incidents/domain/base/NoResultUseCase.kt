package org.rfcx.incidents.domain.base

abstract class NoResultUseCase {
    /**
     * Triggers the execution of this use case
     */
    suspend fun launch() {
        performAction()
    }

    protected abstract fun performAction()
}
