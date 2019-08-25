package org.rfcx.ranger.view.report

import android.content.Context
import org.rfcx.ranger.R
import org.rfcx.ranger.entity.report.Report
import org.rfcx.ranger.util.DateHelper
import java.text.SimpleDateFormat
import java.util.*


fun Report.getImageResource(): Int = when (value) {
	"chainsaw" -> R.drawable.ic_chainsaw
	"vehicle" -> R.drawable.ic_vehicle
	"trespasser" -> R.drawable.ic_people
	"gunshot" -> R.drawable.ic_gun
	else -> R.drawable.ic_other
}

fun Report.getLocalisedValue(context: Context): String = when (value) {
	// TODO: uncomment when translations are available @Tree
//	"chainsaw" -> context.getString(R.string.chainsaw)
//	"vehicle" -> context.getString(R.string.vehicle)
//	"trespasser" -> context.getString(R.string.trespasser)
//	"gunshot" -> context.getString(R.string.gunshot)
	else -> value.capitalize()
}

fun Report.getLocalisedAgeEstimate(context: Context): String = when (getAgeEstimate()) {
	Report.AgeEstimate.NOW -> context.getString(R.string.at_that_time)
	Report.AgeEstimate.LAST_24_HR -> context.getString(R.string.within_24_hours)
	Report.AgeEstimate.LAST_WEEK -> context.getString(R.string.within_week)
	Report.AgeEstimate.LAST_MONTH -> context.getString(R.string.within_month)
	else -> ""
}

fun Report.getReportedAtRelative(context: Context): String {
	val date = DateHelper.getDateTime(reportedAt)
	if (date == null) return ""
	
	val diff = Date().time - date.time
	val dayAgo = DateHelper.DAY
	val daysAgo = 2 * DateHelper.DAY
	return if (diff < dayAgo) {
		val timeFormat = SimpleDateFormat(DateHelper.timeFormat, Locale.US)
		timeFormat.format(date.time)
	} else if (diff < daysAgo) {
		val timeFormat = SimpleDateFormat(DateHelper.timeFormat, Locale.US)
		"${context.getString(R.string.yesterday)} ${timeFormat.format(date.time)}"
	} else {
		val dateFormat = SimpleDateFormat("MMMM d, yyyy - HH:mm", Locale.US)
		dateFormat.format(date.time)
	}
}