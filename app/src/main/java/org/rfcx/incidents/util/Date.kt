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

private val isoTextFormat by lazy {
    val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
    sdf.timeZone = TimeZone.getTimeZone("UTC")
    sdf
}

private const val timeFormat = "HH:mm"
private const val shortDateFormat = "dd MMM yyyy"
private const val standardDateFormat = "MMMM d, yyyy HH:mm"
private const val dateFormat = "d MMM yyyy, HH:mm"
private const val dateFormatWithoutYear = "d MMM, HH:mm"
private const val dateWithTimeZoneFormat = "d MMM yyyy, HH:mm (zzz)"
private const val dateWithTimeZoneWithoutYear = "d MMM, HH:mm (zzz)"

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

private val outputDateWithoutYearSdf by lazy {
    val sdf = SimpleDateFormat(dateFormatWithoutYear, Locale.getDefault())
    sdf.timeZone = TimeZone.getDefault()
    sdf
}

private val outputWithTimeZoneSdf by lazy {
    val sdf = SimpleDateFormat(dateWithTimeZoneFormat, Locale.getDefault())
    sdf.timeZone = TimeZone.getDefault()
    sdf
}

private val outputTimeZoneWithoutYearSdf by lazy {
    val sdf = SimpleDateFormat(dateWithTimeZoneWithoutYear, Locale.getDefault())
    sdf.timeZone = TimeZone.getDefault()
    sdf
}

fun Date.toIsoFormatString(): String {
    return isoFormat.format(this) // pattern 20211128T153441279Z
}

fun Date.toIsoString(): String {
    return isoTextFormat.format(this) // pattern 2008-09-15T15:53:00.000Z
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
    val dateString = isoSdf.format(this)
    val tempSdf = isoSdf
    tempSdf.timeZone = timeZone
    return tempSdf.parse(dateString)
}

fun Date.toTimeWithTimeZone(timeZone: TimeZone?): String {
    val tempSdf = outputWithTimeZoneSdf
    tempSdf.timeZone = timeZone ?: TimeZone.getDefault()
    return tempSdf.format(this)
}

fun Date.toStringWithTimeZone(context: Context, timeZone: TimeZone?): String {
    val tz = timeZone ?: TimeZone.getDefault()
    val outputDate = if (this.isThisYear()) outputDateWithoutYearSdf else outputDateSdf
    val outputWithTimeZone = if (this.isThisYear()) outputTimeZoneWithoutYearSdf else outputWithTimeZoneSdf
    val tempSdf = if (tz == TimeZone.getDefault()) outputDate else outputWithTimeZone
    tempSdf.timeZone = tz
    return (if (tz == TimeZone.getDefault()) tempSdf.format(this) else setShortTimeZone(tempSdf.format(this))).monthTranslate(context)
}

private fun makeToShortString(str: String, symbol: String): String {
    val timeText = str.split(":")
    return if (str.first() == '0') {
        if (timeText[1].first() == '0') {
            "(GMT" + symbol + timeText[0].last() + ")"
        } else {
            "(GMT" + symbol + timeText[0] + ":" + timeText[1].dropLast(1) + ")"
        }
    } else {
        if (timeText[1].first() == '0') {
            "(GMT" + symbol + timeText[0] + ")"
        } else {
            "(GMT" + symbol + timeText[0] + ":" + timeText[1].dropLast(1) + ")"
        }
    }
}

fun setShortTimeZone(str: String): String {
    val start = str.split("(")
    return when {
        start[1].contains("GMT+00") -> start[0] + "(GMT)"
        start[1].contains("GMT+") -> start[0] + makeToShortString(str.split("+").last(), "+")
        start[1].contains("GMT-") -> start[0] + makeToShortString(str.split("-").last(), "-")
        else -> str
    }
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
fun Date.toTimeSinceStringAlternativeTimeAgo(context: Context, timeZone: TimeZone? = TimeZone.getDefault()): String {
    val niceDateStr =
        DateUtils.getRelativeTimeSpanString(
            this.toDateWithTimeZone(timeZone ?: TimeZone.getDefault()).time,
            Calendar.getInstance().timeInMillis,
            DateUtils.MINUTE_IN_MILLIS
        )
    return if (niceDateStr.toString() == "0 minutes ago") {
        context.getString(R.string.report_time_second)
    } else if (niceDateStr.toString() == "Yesterday") {
        "${context.getString(R.string.yesterday)}, ${this.toTimeString()}"
    } else if (!niceDateStr.toString().contains("ago")) {
        this.toStringWithTimeZone(context, timeZone)
    } else if (niceDateStr.toString().contains("days ago")) {
        this.toStringWithTimeZone(context, timeZone)
    } else {
        niceDateStr.toString()
    }
}

fun String.monthTranslate(context: Context): String {
    // Log.d("monthTranslate",this)
    return when {
        this.contains("seconds") -> {
            this.replace("seconds", context.getString(R.string.seconds))
        }
        this.contains("minutes") -> {
            this.replace("minutes", context.getString(R.string.minutes))
        }
        this.contains("hours") -> {
            this.replace("hours", context.getString(R.string.hours))
        }
        this.contains("years") -> {
            this.replace("years", context.getString(R.string.years))
        }
        this.contains("months") -> {
            this.replace("months", context.getString(R.string.months))
        }
        this.contains("days") -> {
            this.replace("days", context.getString(R.string.days))
        }
        this.contains("second") -> {
            this.replace("second", context.getString(R.string.second))
        }
        this.contains("minute") -> {
            this.replace("minute", context.getString(R.string.minute))
        }
        this.contains("hour") -> {
            this.replace("hour", context.getString(R.string.hour))
        }
        this.contains("year") -> {
            this.replace("year", context.getString(R.string.year))
        }
        this.contains("month") -> {
            this.replace("month", context.getString(R.string.month))
        }
        this.contains("day") -> {
            this.replace("day", context.getString(R.string.day))
        }
        this.contains("Jan") -> {
            this.replace("Jan", context.getString(R.string.jan))
        }
        this.contains("Feb") -> {
            this.replace("Feb", context.getString(R.string.feb))
        }
        this.contains("Mar") -> {
            this.replace("Mar", context.getString(R.string.mar))
        }
        this.contains("Apr") -> {
            this.replace("Apr", context.getString(R.string.apr))
        }
        this.contains("May") -> {
            this.replace("May", context.getString(R.string.may))
        }
        this.contains("Jun") -> {
            this.replace("Jun", context.getString(R.string.jun))
        }
        this.contains("Jul") -> {
            this.replace("Jul", context.getString(R.string.jul))
        }
        this.contains("Aug") -> {
            this.replace("Aug", context.getString(R.string.aug))
        }
        this.contains("Sep") -> {
            this.replace("Sep", context.getString(R.string.sep))
        }
        this.contains("Oct") -> {
            this.replace("Oct", context.getString(R.string.oct))
        }
        this.contains("Nov") -> {
            this.replace("Nov", context.getString(R.string.nov))
        }
        this.contains("Dec") -> {
            this.replace("Dec", context.getString(R.string.dec))
        }
        else -> {
            this
        }
    }
}
