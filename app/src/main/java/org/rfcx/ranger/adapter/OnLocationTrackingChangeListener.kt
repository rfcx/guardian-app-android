package org.rfcx.ranger.adapter

/**
 * Responds when the location tracking (on/off) is changed by the user
 */

interface OnLocationTrackingChangeListener {
    fun isEnableTracking(): Boolean

    fun onLocationTrackingChange(on: Boolean)
}