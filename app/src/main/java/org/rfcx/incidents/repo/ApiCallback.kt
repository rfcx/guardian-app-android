package org.rfcx.incidents.repo

interface ApiCallback {
    fun onFailed(t: Throwable?, message: String?)
}
