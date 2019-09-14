package org.rfcx.ranger.data.remote.domain

import io.reactivex.observers.DisposableSingleObserver
import org.rfcx.ranger.data.remote.Result
import org.rfcx.ranger.util.getResultError

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