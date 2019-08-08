package org.rfcx.ranger.util

import org.rfcx.ranger.R
import org.rfcx.ranger.entity.event.Event

fun String.toEventPosition():Int {
   return when(this) {
        Event.chainsaw -> 2
        Event.gunshot -> 3
        Event.vehicle -> 0
        Event.trespasser -> 1
        Event.other -> 4
        else -> -1
    }
}

fun String.toEventIcon(): Int {
    return when (this) {
        Event.chainsaw -> R.drawable.ic_chainsaw
        Event.gunshot -> R.drawable.ic_gun
        Event.vehicle -> R.drawable.ic_truck
        Event.trespasser -> R.drawable.ic_people
        else -> R.drawable.ic_other
    }
}