package org.rfcx.incidents.domain.base

import io.reactivex.observers.DisposableSingleObserver
import org.rfcx.incidents.data.remote.common.Result
import org.rfcx.incidents.data.remote.common.getResultError

abstract class BaseDisposableSingle<T> : DisposableSingleObserver<T>() {

    override fun onSuccess(t: T) {
        onSuccess(Result.Success(t))
    }

    override fun onError(e: Throwable) {
        onError(e, e.getResultError())
    }

    abstract fun onSuccess(success: Result<T>)

    abstract fun onError(e: Throwable, error: Result<T>)
}
