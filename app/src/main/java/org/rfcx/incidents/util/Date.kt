package org.rfcx.incidents.util

import android.annotation.SuppressLint
import android.content.Context
import android.text.format.DateUtils
import org.rfcx.incidents.R
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

private const val SECONDS_TEXT = "seconds"
private const val MINUTES_TEXT = "minutes"
private const val HOURS_TEXT = "hours"
private const val YEARS_TEXT = "years"
private const val MONTHS_TEXT = "months"
private const val DAYS_TEXT = "days"
private const val SECOND_TEXT = "second"
private const val MINUTE_TEXT = "minute"
private const val HOUR_TEXT = "hour"
private const val YEAR_TEXT = "year"
private const val MONTH_TEXT = "month"
private const val DAY_TEXT = "day"
private const val JAN = "Jan"
private const val FEB = "Feb"
private const val MAR = "Mar"
private const val APR = "Apr"
private const val MAY = "May"
private const val JUN = "Jun"
private const val JUL = "Jul"
private const val AUG = "Aug"
private const val SEP = "Sep"
private const val OCT = "Oct"
private const val NOV = "Nov"
private const val DEC = "Dec"

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
    return when {
        this.contains(SECONDS_TEXT) -> {
            this.replace(SECONDS_TEXT, context.getString(R.string.seconds))
        }
        this.contains(MINUTES_TEXT) -> {
            this.replace(MINUTES_TEXT, context.getString(R.string.minutes))
        }
        this.contains(HOURS_TEXT) -> {
            this.replace(HOURS_TEXT, context.getString(R.string.hours))
        }
        this.contains(YEARS_TEXT) -> {
            this.replace(YEARS_TEXT, context.getString(R.string.years))
        }
        this.contains(MONTHS_TEXT) -> {
            this.replace(MONTHS_TEXT, context.getString(R.string.months))
        }
        this.contains(DAYS_TEXT) -> {
            this.replace(DAYS_TEXT, context.getString(R.string.days))
        }
        this.contains(SECOND_TEXT) -> {
            this.replace(SECOND_TEXT, context.getString(R.string.second))
        }
        this.contains(MINUTE_TEXT) -> {
            this.replace(MINUTE_TEXT, context.getString(R.string.minute))
        }
        this.contains(HOUR_TEXT) -> {
            this.replace(HOUR_TEXT, context.getString(R.string.hour))
        }
        this.contains(YEAR_TEXT) -> {
            this.replace(YEAR_TEXT, context.getString(R.string.year))
        }
        this.contains(MONTH_TEXT) -> {
            this.replace(MONTH_TEXT, context.getString(R.string.month))
        }
        this.contains(DAY_TEXT) -> {
            this.replace(DAY_TEXT, context.getString(R.string.day))
        }
        this.contains(JAN) -> {
            this.replace(JAN, context.getString(R.string.jan))
        }
        this.contains(FEB) -> {
            this.replace(FEB, context.getString(R.string.feb))
        }
        this.contains(MAR) -> {
            this.replace(MAR, context.getString(R.string.mar))
        }
        this.contains(APR) -> {
            this.replace(APR, context.getString(R.string.apr))
        }
        this.contains(MAY) -> {
            this.replace(MAY, context.getString(R.string.may))
        }
        this.contains(JUN) -> {
            this.replace(JUN, context.getString(R.string.jun))
        }
        this.contains(JUL) -> {
            this.replace(JUL, context.getString(R.string.jul))
        }
        this.contains(AUG) -> {
            this.replace(AUG, context.getString(R.string.aug))
        }
        this.contains(SEP) -> {
            this.replace(SEP, context.getString(R.string.sep))
        }
        this.contains(OCT) -> {
            this.replace(OCT, context.getString(R.string.oct))
        }
        this.contains(NOV) -> {
            this.replace(NOV, context.getString(R.string.nov))
        }
        this.contains(DEC) -> {
            this.replace(DEC, context.getString(R.string.dec))
        }
        else -> {
            this
        }
    }
}
