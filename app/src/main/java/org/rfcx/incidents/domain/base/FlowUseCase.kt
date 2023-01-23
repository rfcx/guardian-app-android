package org.rfcx.incidents.domain.base

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest

/**
 * Simple use case exposing result as a flow.
 * Result flow will emit null while the action has not been triggered
 */
@OptIn(ExperimentalCoroutinesApi::class)
abstract class FlowUseCase<T> {

    private val _trigger = MutableStateFlow(true)

    /**
     * Exposes result of this use case
     */
    val resultFlow: Flow<T> = _trigger.flatMapLatest {
        performAction()
    }
    /**
     * Triggers the execution of this use case
     */
    suspend fun launch() {
        _trigger.emit(!(_trigger.value))
    }

    protected abstract fun performAction() : Flow<T>
}
