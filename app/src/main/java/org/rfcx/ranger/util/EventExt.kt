package org.rfcx.ranger.util

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import com.google.protobuf.ByteString
import org.rfcx.ranger.R
import org.rfcx.ranger.adapter.entity.BaseItem
import org.rfcx.ranger.entity.event.Event
import org.rfcx.ranger.entity.event.ReviewEventFactory
import kotlin.math.absoluteValue

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

fun Event.toEventItem(state: String?): EventItem {
	val s = when (state) {
		ReviewEventFactory.confirmEvent -> EventItem.State.CONFIRM
		ReviewEventFactory.rejectEvent -> EventItem.State.REJECT
		else -> EventItem.State.NONE
	}
	return EventItem(this, s)
}

fun Event.latitudeAsDMS(decimalPlace: Int): String {
	val direction = if (this.latitude!! > 0) "N" else "S"
	var strLatitude = Location.convert(latitude!!.absoluteValue, Location.FORMAT_SECONDS)
	strLatitude = replaceDelimiters(strLatitude, decimalPlace)
	strLatitude += " $direction"
	return strLatitude
}

fun Event.longitudeAsDMS(decimalPlace: Int): String {
	val direction = if (this.longitude!! > 0) "W" else "E"
	var strLongitude = Location.convert(this.longitude!!.absoluteValue, Location.FORMAT_SECONDS)
	strLongitude = replaceDelimiters(strLongitude, decimalPlace)
	strLongitude += " $direction"
	return strLongitude
}

private fun replaceDelimiters(str: String, decimalPlace: Int): String {
	var str = str
	str = str.replaceFirst(":".toRegex(), "°")
	str = str.replaceFirst(":".toRegex(), "'")
	val pointIndex = str.indexOf(".")
	val endIndex = pointIndex + 1 + decimalPlace
	if (endIndex < str.length) {
		str = str.substring(0, endIndex)
	}
	str += "\""
	return str
}

data class EventItem(var event: Event, var state: State = State.NONE) : BaseItem {
	
	@SuppressLint("DefaultLocale")
	fun getReviewerName(context: Context) : String {
		return if (state != State.NONE) {
			if (event.firstNameReviewer.isNotBlank()) {
				event.firstNameReviewer
			} else {
				context.getNameEmail()
			}.capitalize()
		} else {
			event.firstNameReviewer.capitalize()
		}
	}
	
	enum class State {
		CONFIRM, REJECT, NONE
	}
}