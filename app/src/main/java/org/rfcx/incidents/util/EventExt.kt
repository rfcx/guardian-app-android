package org.rfcx.incidents.util

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import org.rfcx.incidents.R
import org.rfcx.incidents.adapter.entity.BaseItem
import org.rfcx.incidents.entity.event.Event
import org.rfcx.incidents.entity.event.ReviewEventFactory
import org.rfcx.incidents.view.profile.coordinates.CoordinatesActivity.Companion.DDM_FORMAT
import org.rfcx.incidents.view.profile.coordinates.CoordinatesActivity.Companion.DD_FORMAT
import org.rfcx.incidents.view.profile.coordinates.CoordinatesActivity.Companion.DMS_FORMAT
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

fun Event.locationCoordinates(context: Context): String? {
	if (latitude != null && longitude != null) {
		val directionLatitude = if (latitude!! > 0) "N" else "S"
		val directionLongitude = if (longitude!! > 0) "E" else "W"
		
		var strLatitude = ""
		var strLongitude = ""
		
		when (context.getCoordinatesFormat()) {
			DD_FORMAT -> {
				strLatitude = Location.convert(latitude!!.absoluteValue, Location.FORMAT_DEGREES)
				strLongitude = Location.convert(longitude!!.absoluteValue, Location.FORMAT_DEGREES)
				
				strLatitude = "${replaceDelimitersDD(strLatitude)}$directionLatitude"
				strLongitude = "${replaceDelimitersDD(strLongitude)}$directionLongitude"
			}
			DDM_FORMAT -> {
				strLatitude = Location.convert(latitude!!.absoluteValue, Location.FORMAT_MINUTES)
				strLongitude = Location.convert(longitude!!.absoluteValue, Location.FORMAT_MINUTES)
				
				strLatitude = "${replaceDelimitersDDM(strLatitude)}$directionLatitude"
				strLongitude = "${replaceDelimitersDDM(strLongitude)}$directionLongitude"
			}
			DMS_FORMAT -> {
				strLatitude = Location.convert(latitude!!.absoluteValue, Location.FORMAT_SECONDS)
				strLongitude = Location.convert(longitude!!.absoluteValue, Location.FORMAT_SECONDS)
				
				strLatitude = "${replaceDelimitersDMS(strLatitude)}$directionLatitude"
				strLongitude = "${replaceDelimitersDMS(strLongitude)}$directionLongitude"
			}
		}
		return StringBuilder(strLatitude)
				.append(", ")
				.append(strLongitude).toString()
	}
	return null
}

private fun replaceDelimitersDMS(str: String): String {
	var strDMSFormat = str
	strDMSFormat = strDMSFormat.replaceFirst(":".toRegex(), "°")
	strDMSFormat = strDMSFormat.replaceFirst(":".toRegex(), "'")
	val pointIndex = strDMSFormat.indexOf(".")
	val endIndex = pointIndex + 2
	if (endIndex < strDMSFormat.length) {
		strDMSFormat = strDMSFormat.substring(0, endIndex)
	}
	strDMSFormat += "\""
	return strDMSFormat
}

private fun replaceDelimitersDD(str: String): String {
	var strDDFormat = str
	val pointIndex = strDDFormat.indexOf(".")
	val endIndex = pointIndex + 6
	if (endIndex < strDDFormat.length) {
		strDDFormat = strDDFormat.substring(0, endIndex)
	}
	strDDFormat += "°"
	return strDDFormat
}

private fun replaceDelimitersDDM(str: String): String {
	var strDDMFormat = str
	strDDMFormat = strDDMFormat.replaceFirst(":".toRegex(), "°")
	val pointIndex = strDDMFormat.indexOf(".")
	val endIndex = pointIndex + 5
	if (endIndex < strDDMFormat.length) {
		strDDMFormat = strDDMFormat.substring(0, endIndex)
	}
	strDDMFormat += "\'"
	return strDDMFormat
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
