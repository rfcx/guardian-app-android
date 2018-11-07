package org.rfcx.ranger.adapter

/**
 * Responds when the location tracking (on/off) is changed by the user
 */

interface OnLocationTrackingChangeListener {
    fun onLocationTrackingChange(on: Boolean)
}