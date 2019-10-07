package org.rfcx.ranger.view.report

import android.content.Context
import org.rfcx.ranger.R
import org.rfcx.ranger.entity.event.Event
import org.rfcx.ranger.entity.report.Report
import org.rfcx.ranger.util.DateHelper
import org.rfcx.ranger.util.formatFullDate
import org.rfcx.ranger.util.formatTime
import java.util.*


fun Report.getImageResource(): Int = when (value) {
	Event.chainsaw -> R.drawable.ic_chainsaw
	Event.vehicle -> R.drawable.ic_vehicle
	Event.trespasser -> R.drawable.ic_people
	Event.gunshot -> R.drawable.ic_gun
	else -> R.drawable.ic_pin_huge
}

fun Report.getLocalisedValue(context: Context): String = when (value) {
	Event.chainsaw -> context.getString(R.string.chainsaw)
	Event.vehicle -> context.getString(R.string.vehicle)
	Event.trespasser -> context.getString(R.string.trespasser)
	Event.gunshot -> context.getString(R.string.gunshot)
	Event.other -> context.getString(R.string.other)
	else -> value
}.capitalize()

fun Report.getLocalisedAgeEstimate(context: Context): String = when (getAgeEstimate()) {
	Report.AgeEstimate.NOW -> context.getString(R.string.at_that_time)
	Report.AgeEstimate.LAST_24_HR -> context.getString(R.string.within_24_hours)
	Report.AgeEstimate.LAST_WEEK -> context.getString(R.string.within_week)
	Report.AgeEstimate.LAST_MONTH -> context.getString(R.string.within_month)
	else -> ""
}

fun Report.getReportedAtRelative(context: Context): String {
	val diff = Date().time - reportedAt.time
	val dayAgo = DateHelper.DAY
	val daysAgo = 2 * DateHelper.DAY
	return if (diff < dayAgo) {
		reportedAt.formatTime()
	} else if (diff < daysAgo) {
		"${context.getString(R.string.yesterday)} ${reportedAt.formatTime()}"
	} else {
		reportedAt.formatFullDate()
	}
}