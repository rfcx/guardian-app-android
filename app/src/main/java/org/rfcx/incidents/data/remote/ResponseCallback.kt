package org.rfcx.incidents.data.remote

interface ResponseCallback<T> {
	fun onSuccess(t: T)
	fun onError(e: Throwable)
}
