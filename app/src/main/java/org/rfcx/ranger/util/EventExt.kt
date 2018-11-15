package org.rfcx.ranger.util

import org.rfcx.ranger.R
import org.rfcx.ranger.entity.event.Event

fun Event.getIconRes(): Int {
	
	return when (this.value) {
		Event.chainsaw -> R.drawable.ic_chainsaw
		Event.gunshot -> R.drawable.ic_gun
		Event.vehicle -> R.drawable.ic_truck
		Event.trespasser -> R.drawable.ic_people
		else -> R.drawable.ic_other
	}
}