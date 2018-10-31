package org.rfcx.ranger.repo

interface ApiCallback {
    fun onFailed(t: Throwable?, message: String?)
}