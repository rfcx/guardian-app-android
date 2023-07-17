package org.rfcx.incidents.data.remote.common

class SoftwareNotCompatibleException : Exception() {
    override val message: String
        get() = "Your guardian software version is not compatible with this feature."
}

class GuardianModeNotCompatibleException : Exception() {
    override val message: String
        get() = "Your device is configured for Cell/GSM. Please switch to SMS, Satellite or Offline mode to activate a classifier."
}

class NoActiveClassifierException : Exception()

class OperationTimeoutException : Exception()

class NoConnectionException : Exception() {
    override val message: String
        get() = "There is no internet connection"
}
