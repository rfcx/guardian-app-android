package org.rfcx.ranger.adapter

import org.rfcx.ranger.service.NetworkState

/**
 * Responds when the location tracking (on/off) is changed by the user
 */

interface OnLocationTrackingChangeListener {
    fun isEnableTracking(): Boolean
    fun getNetworkState(): NetworkState

    fun onLocationTrackingChange(on: Boolean)
}