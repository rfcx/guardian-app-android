package org.rfcx.ranger.util

import org.rfcx.ranger.R
import org.rfcx.ranger.entity.event.Event


data class EventIcon (val event: Event) {

    fun resId(isOpened: Boolean = false): Int {
	    return when {
		    Event.chainsaw.equals(event.value, true) -> {
			    if (isOpened) R.drawable.chainsaw_grey else R.drawable.chainsaw_orange
		    }
		    Event.gunshot.equals(event.value, true) -> {
			    if (isOpened) R.drawable.gun_grey else R.drawable.gun_orange
		    }
		    Event.vehicle.equals(event.value, true) -> {
			    if (isOpened) R.drawable.vehicle_grey else R.drawable.vehicle_orange
		    }
		    else -> {
			    if (isOpened) R.drawable.event_grey else R.drawable.event_orange
		    }
	    }
    }
}