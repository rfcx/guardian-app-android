package org.rfcx.incidents.data.remote.common

interface ApiCallback {
    fun onFailed(t: Throwable?, message: String?)
}
