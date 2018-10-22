package android.rfcx.org.ranger.util

import android.rfcx.org.ranger.R
import android.rfcx.org.ranger.entity.event.Event


data class EventIcon (val event: Event) {

    fun resId(isOpened: Boolean = false): Int {
        when {
            Event.chainsaw.equals(event.value, true) -> {
                return if (isOpened) R.drawable.chainsaw_grey else R.drawable.chainsaw_orange
            }
            Event.gunshot.equals(event.value, true) -> {
                return if (isOpened) R.drawable.gun_grey else R.drawable.gun_orange
            }
            Event.vehicle.equals(event.value, true) -> {
                return if (isOpened) R.drawable.vehicle_grey else R.drawable.vehicle_orange
            }
            else -> {
                return if (isOpened) R.drawable.event_grey else R.drawable.event_orange
            }
        }
    }
}