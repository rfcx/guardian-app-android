package org.rfcx.ranger.util

import android.content.Context
import org.rfcx.ranger.R
import org.rfcx.ranger.entity.event.Event
import org.rfcx.ranger.util.DateHelper.DAY
import java.text.SimpleDateFormat
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
	beginsAt ?: return ""
	
	val eventDate = DateHelper.getDateTime(beginsAt)
	// timestamp
	val dayAgo = DAY
	val daysAgo = 2 * DAY
	
	val diff = Date().time - eventDate!!.time
	
	val timeFormat = SimpleDateFormat(DateHelper.timeFormat, Locale.US)
	
	return if (diff < dayAgo) {
		timeFormat.format(eventDate.time)
	} else if (diff < daysAgo) {
		"${context.getString(R.string.yesterday)} ${timeFormat.format(eventDate.time)}"
	} else {
		val dateFormat = SimpleDateFormat("MMMM d, yyyy HH:mm", Locale.US)
		dateFormat.format(eventDate.time)
	}
}