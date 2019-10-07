package org.rfcx.ranger.util

import android.content.Context
import org.joda.time.Duration
import org.rfcx.ranger.R
import org.rfcx.ranger.entity.event.Event
import java.util.*

fun Event.getIconRes(): Int {
	
	return when (this.value) {
		Event.chainsaw -> R.drawable.ic_chainsaw
		Event.gunshot -> R.drawable.ic_gun
		Event.vehicle -> R.drawable.ic_vehicle
		Event.trespasser -> R.drawable.ic_people
		else -> R.drawable.ic_pin_huge
	}
}

fun String.toEventPosition(): Int {
	return when (this) {
		Event.chainsaw -> 2
		Event.gunshot -> 3
		Event.vehicle -> 0
		Event.trespasser -> 1
		Event.other -> 4
		else -> -1
	}
}

fun String.toEventName(context: Context): String = when (this) {
	Event.chainsaw -> context.getString(R.string.chainsaw)
	Event.gunshot -> context.getString(R.string.gunshot)
	Event.vehicle -> context.getString(R.string.vehicle)
	Event.trespasser -> context.getString(R.string.trespasser)
	Event.other -> context.getString(R.string.other)
	else -> this
}.capitalize()

fun String.toEventIcon(): Int {
	return when (this) {
		Event.chainsaw -> R.drawable.ic_chainsaw
		Event.gunshot -> R.drawable.ic_gun
		Event.vehicle -> R.drawable.ic_vehicle
		Event.trespasser -> R.drawable.ic_people
		else -> R.drawable.ic_pin_huge
	}
}

fun Event.timeAgoDisplay(context: Context): String { // TODO this needs refactoring
	
	val diff = Duration(beginsAt.time, Date().time).standardHours
	return if (beginsAt.isToday()) {
		beginsAt.formatTime()
	} else if (diff < 48) {
		"${context.getString(R.string.yesterday)} ${beginsAt.formatTime()}"
	} else {
		DateHelper.formatFullDate(beginsAt)
	}
}