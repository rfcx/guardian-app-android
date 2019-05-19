package org.rfcx.ranger.util

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