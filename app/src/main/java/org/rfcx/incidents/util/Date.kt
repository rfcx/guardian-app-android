package org.rfcx.incidents.util

import android.annotation.SuppressLint
import android.content.Context
import android.text.format.DateUtils
import org.rfcx.incidents.R
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

private val isoSdf by lazy {
    val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
    sdf.timeZone = TimeZone.getTimeZone("UTC")
    sdf
}

private val isoFormat by lazy {
    val sdf = SimpleDateFormat("yyyyMMdd'T'HHmmssSSS'Z'", Locale.US)
    sdf.timeZone = TimeZone.getTimeZone("UTC")
    sdf
}

private const val timeFormat = "HH:mm"
private const val shortDateFormat = "dd MMM yyyy"
private const val standardDateFormat = "MMMM d, yyyy HH:mm"
private const val dateFormat = "d MMM yyyy, HH:mm"

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
private val outputDateSdf by lazy {
    val sdf = SimpleDateFormat(dateFormat, Locale.getDefault())
    sdf.timeZone = TimeZone.getDefault()
    sdf
}

fun Date.toIsoString(): String {
    return isoSdf.format(this) // pattern 2008-09-15T15:53:00.000Z
}

fun Date.toIsoFormatString(): String {
    return isoFormat.format(this) // pattern 20211128T153441279Z
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

fun Date.toDateTimeString(): String {
    return outputDateSdf.format(this)
}

fun Date.millisecondsSince(): Long {
    return Date().time - this.time
}

fun Date.toDateWithTimeZone(timeZone: TimeZone): Date {
    val dateString = this.toIsoString()
    val tempSdf = isoSdf
    tempSdf.timeZone = timeZone
    return tempSdf.parse(dateString)
}

fun Date.toTimeWithTimeZone(timeZone: TimeZone): String {
    val tempSdf = outputTimeSdf
    tempSdf.timeZone = timeZone
    return tempSdf.format(this)
}

fun Date.toStringWithTimeZone(timeZone: TimeZone): String {
    val tempSdf = outputDateSdf
    tempSdf.timeZone = timeZone
    return tempSdf.format(this)
}

private val legacyInputFormatters by lazy {
    arrayListOf(
        isoSdf,
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZZZZZ", Locale.US),
        SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
    )
}

@Deprecated(message = "Only used for migrating old dates from Realm.")
fun legacyDateParser(input: String?): Date? {
    input ?: return null

    legacyInputFormatters.forEach {
        try {
            return it.parse(input)
        } catch (e: ParseException) {
        }
    }
    return null // not found format matching
}

// TODO Moved from Common, is it still needed?
private const val SECOND: Long = 1000
private const val MINUTE = 60 * SECOND
private const val HOUR = 60 * MINUTE
private const val DAY = 24 * HOUR
private const val WEEK = 7 * DAY

@SuppressLint("SimpleDateFormat")
fun Date.toTimeSinceStringAlternativeTimeAgo(context: Context, timeZone: TimeZone = TimeZone.getDefault()): String {
    val niceDateStr =
        DateUtils.getRelativeTimeSpanString(this.toDateWithTimeZone(timeZone).time, Calendar.getInstance().timeInMillis, DateUtils.MINUTE_IN_MILLIS)

    return if (niceDateStr.toString() == "0 minutes ago") {
        context.getString(R.string.report_time_second)
    } else if (niceDateStr.toString() == "Yesterday") {
        "${context.getString(R.string.yesterday)} ${this.toTimeWithTimeZone(timeZone)}"
    } else if (!niceDateStr.toString().contains("ago")) {
        this.toStringWithTimeZone(timeZone)
    } else if (niceDateStr.toString().contains("days ago")) {
        this.toStringWithTimeZone(timeZone)
    } else {
        niceDateStr.toString()
    }
}
