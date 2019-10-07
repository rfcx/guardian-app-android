package org.rfcx.ranger.util

import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*


private val isoSdf by lazy {
	val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
	sdf.timeZone = TimeZone.getTimeZone("UTC")
	sdf
}

private const val timeFormat = "HH:mm"
private const val shortDateFormat = "dd MMM yyyy"
private const val standardDateFormat = "MMMM d, yyyy HH:mm"

private val outputTimeSdf by lazy {
	val sdf = SimpleDateFormat(timeFormat, Locale.getDefault())
	sdf.timeZone = TimeZone.getDefault()
	sdf
}
private val outputShortDateSdf by lazy {
	val sdf = SimpleDateFormat(shortDateFormat, Locale.getDefault())
	sdf.timeZone = TimeZone.getDefault()
	sdf
}
private val outputStandardDateSdf by lazy {
	val sdf = SimpleDateFormat(standardDateFormat, Locale.getDefault())
	sdf.timeZone = TimeZone.getDefault()
	sdf
}



fun Date.toIsoString(): String {
	return isoSdf.format(this) // pattern 2008-09-15T15:53:00.000Z
}

fun Date.toTimeString(): String {
	return outputTimeSdf.format(this)
}

fun Date.toShortDateString(): String {
	return outputShortDateSdf.format(this)
}

fun Date.toFullDateTimeString(): String {
	return outputStandardDateSdf.format(this)
}

fun Date.millisecondsSince(): Long {
	return Date().time - this.time
}


object DateHelper {
	private const val SECOND: Long = 1000
	const val MINUTE = 60 * SECOND
	const val HOUR = 60 * MINUTE
	const val DAY = 24 * HOUR
	const val WEEK = 7 * DAY
}
	
	
private val legacyInputFormatters by lazy { arrayListOf(
	isoSdf,
	SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZZZZZ", Locale.US),
	SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US))
}

@Deprecated(message = "Only used for migrating old dates from Realm.")
fun legacyDateParser(input: String?): Date? {
	input ?: return null
	
	legacyInputFormatters.forEach {
		try {
			return it.parse(input)
		} catch (e: ParseException) { }
	}
	return null // not found format matching
}
