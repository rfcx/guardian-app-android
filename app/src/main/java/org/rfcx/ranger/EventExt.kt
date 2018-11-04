package org.rfcx.ranger

import org.rfcx.ranger.entity.event.Event

fun Event.getIconRes(): Int {
	
	return when (this.value) {
		Event.chainsaw -> R.drawable.ic_chainsaw
		Event.gunshot -> R.drawable.ic_gun
		Event.vehicle -> R.drawable.ic_truck
		Event.people -> R.drawable.ic_people
		else -> R.drawable.ic_other
	}
}