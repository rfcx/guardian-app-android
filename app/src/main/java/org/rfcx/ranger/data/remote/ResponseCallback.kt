package org.rfcx.ranger.data.remote

interface ResponseCallback<T> {
	fun onSuccess(t: T)
	fun onError(e: Throwable)
}