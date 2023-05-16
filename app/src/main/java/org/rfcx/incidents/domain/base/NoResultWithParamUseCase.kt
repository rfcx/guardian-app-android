package org.rfcx.incidents.domain.base

abstract class NoResultWithParamUseCase<in Params> {
    /**
     * Triggers the execution of this use case
     */
    fun launch(param: Params) {
        return performAction(param)
    }

    protected abstract fun performAction(param: Params)
}
