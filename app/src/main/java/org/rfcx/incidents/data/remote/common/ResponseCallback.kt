package org.rfcx.incidents.data.remote.common

interface ResponseCallback<T> {
    fun onSuccess(t: T)
    fun onError(e: Throwable)
}
