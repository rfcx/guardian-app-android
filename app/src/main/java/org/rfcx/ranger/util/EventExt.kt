package org.rfcx.ranger.util

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import org.rfcx.ranger.R
import org.rfcx.ranger.adapter.entity.BaseItem
import org.rfcx.ranger.entity.event.Event
import org.rfcx.ranger.entity.event.ReviewEventFactory
import org.rfcx.ranger.view.profile.coordinates.CoordinatesActivity.Companion.DDM_FORMAT
import org.rfcx.ranger.view.profile.coordinates.CoordinatesActivity.Companion.DD_FORMAT
import org.rfcx.ranger.view.profile.coordinates.CoordinatesActivity.Companion.DMS_FORMAT
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

fun Event.locationCoordinates(context: Context): String {
	val directionLatitude = if (latitude!! > 0) "N" else "S"
	val directionLongitude = if (longitude!! > 0) "E" else "W"
	
	var strLatitude = ""
	var strLongitude = ""
	
	when (context.getCoordinatesFormat()) {
		DD_FORMAT -> {
			strLatitude = Location.convert(latitude!!.absoluteValue, Location.FORMAT_DEGREES)
			strLongitude = Location.convert(longitude!!.absoluteValue, Location.FORMAT_DEGREES)
			
			strLatitude = "${replaceDelimitersDD(strLatitude, 5)}$directionLatitude"
			strLongitude = "${replaceDelimitersDD(strLongitude, 5)}$directionLongitude"
		}
		DDM_FORMAT -> {
			strLatitude = Location.convert(latitude!!.absoluteValue, Location.FORMAT_MINUTES)
			strLongitude = Location.convert(longitude!!.absoluteValue, Location.FORMAT_MINUTES)
			
			strLatitude = "${replaceDelimitersDDM(strLatitude, 4)}$directionLatitude"
			strLongitude = "${replaceDelimitersDDM(strLongitude, 4)}$directionLongitude"
		}
		DMS_FORMAT -> {
			strLatitude = Location.convert(latitude!!.absoluteValue, Location.FORMAT_SECONDS)
			strLongitude = Location.convert(longitude!!.absoluteValue, Location.FORMAT_SECONDS)
			
			strLatitude = "${replaceDelimitersDMS(strLatitude, 1)}$directionLatitude"
			strLongitude = "${replaceDelimitersDMS(strLongitude, 1)}$directionLongitude"
		}
	}
	
	val location = StringBuilder(strLatitude)
			.append(", ")
			.append(strLongitude)
	
	return location.toString()
}

private fun replaceDelimitersDMS(str: String, decimalPlace: Int): String {
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

private fun replaceDelimitersDD(str: String, decimalPlace: Int): String {
	var str = str
	val pointIndex = str.indexOf(".")
	val endIndex = pointIndex + 1 + decimalPlace
	if (endIndex < str.length) {
		str = str.substring(0, endIndex)
	}
	str += "°"
	return str
}

private fun replaceDelimitersDDM(str: String, decimalPlace: Int): String {
	var str = str
	str = str.replaceFirst(":".toRegex(), "°")
	val pointIndex = str.indexOf(".")
	val endIndex = pointIndex + 1 + decimalPlace
	if (endIndex < str.length) {
		str = str.substring(0, endIndex)
	}
	str += "\'"
	return str
}

data class EventItem(var event: Event, var state: State = State.NONE) : BaseItem {
	
	@SuppressLint("DefaultLocale")
	fun getReviewerName(context: Context): String {
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