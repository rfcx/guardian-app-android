package org.rfcx.incidents.adapter

import org.rfcx.incidents.service.NetworkState

/**
 * Responds when the location tracking (on/off) is changed by the user
 */

interface HeaderProtocol {
    fun isEnableTracking(): Boolean
    fun getNetworkState(): NetworkState
    fun getSyncInfo(): SyncInfo?

    fun onLocationTrackingChange(on: Boolean)
    fun onPressCancelSync()
}

/**
 * @param status is current status of sync
 * @param countReport is count unsent
*/
data class SyncInfo(val status: Status = Status.WAITING_NETWORK, val countReport: Long = 1, val countCheckIn: Long = 0) {
    enum class Status { WAITING_NETWORK, STARTING, UPLOADING, UPLOADED }
}
