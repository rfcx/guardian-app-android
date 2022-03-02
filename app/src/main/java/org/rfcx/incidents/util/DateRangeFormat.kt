package org.rfcx.incidents.util

import android.annotation.SuppressLint
import android.content.Context
import android.text.format.DateUtils
import org.rfcx.incidents.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.TimeZone

private const val timeFormat = "HH:mm"
private const val dateShortFormat = "dd MMM"
private const val timeZoneFormat = "(zzz)"

fun dateRangeFormat(context: Context, fromUtc: Date, toUtc: Date, timezone: TimeZone? = null): String {
    val text: String
    val timeZoneText = if (timezone == TimeZone.getDefault()) "" else toUtc.toTimeZoneString(timezone)

    if (isToday(fromUtc.time) && isToday(toUtc.time)) {
        text = sameDateFormat(context.getString(R.string.today), fromUtc.toTimeTextString(timezone), toUtc.toTimeTextString(timezone), timeZoneText)
    } else if (isYesterday(fromUtc.time)) {
        text = if (isToday(toUtc.time)) {
            "${context.getString(R.string.yesterday)} ${fromUtc.toTimeTextString(timezone)} - ${context.getString(R.string.today)} " +
                "${toUtc.toTimeTextString(timezone)} $timeZoneText"
        } else {
            sameDateFormat(context.getString(R.string.yesterday), fromUtc.toTimeTextString(timezone), toUtc.toTimeTextString(timezone), timeZoneText)
        }
    } else {
        text = when {
            isToday(toUtc.time) -> {
                otherDateFormat(fromUtc.toShortDateString(timezone), context.getString(R.string.today), toUtc.toTimeTextString(timezone), timeZoneText)
            }
            isYesterday(toUtc.time) -> {
                otherDateFormat(fromUtc.toShortDateString(timezone), context.getString(R.string.yesterday), toUtc.toTimeTextString(timezone), timeZoneText)
            }
            isSameDate(fromUtc, toUtc) -> {
                "${fromUtc.toShortDateString(timezone)}, ${fromUtc.toTimeTextString(timezone)} - ${toUtc.toTimeTextString(timezone)} $timeZoneText"
            }
            else -> {
                "${fromUtc.toShortDateString(timezone)} - ${toUtc.toShortDateString(timezone)} $timeZoneText"
            }
        }
    }
    return text
}

private fun sameDateFormat(date: String, from: String, to: String, timezone: String): String {
    return "$date, $from - $to $timezone"
}

private fun otherDateFormat(from: String, date: String, to: String, timezone: String): String {
    return "$from - $date, $to $timezone"
}

private fun isYesterday(whenInMillis: Long): Boolean {
    return DateUtils.isToday(whenInMillis + DateUtils.DAY_IN_MILLIS)
}

private fun isToday(whenInMillis: Long): Boolean {
    return DateUtils.isToday(whenInMillis)
}

@SuppressLint("SimpleDateFormat")
private fun isSameDate(startAt: Date, endAt: Date): Boolean {
    val sdf = SimpleDateFormat("yyMMdd")
    return sdf.format(startAt) == sdf.format(endAt)
}

private fun Date.toTimeTextString(timezone: TimeZone?): String {
    val outputTimeSdf = SimpleDateFormat(timeFormat)
    outputTimeSdf.timeZone = timezone ?: TimeZone.getDefault()
    return outputTimeSdf.format(this)
}

private fun Date.toShortDateString(timezone: TimeZone?): String {
    val outputDateShortFormatSdf = SimpleDateFormat(dateShortFormat)
    outputDateShortFormatSdf.timeZone = timezone ?: TimeZone.getDefault()
    return outputDateShortFormatSdf.format(this)
}

private fun Date.toTimeZoneString(timezone: TimeZone?): String {
    val outputDateShortFormatSdf = SimpleDateFormat(timeZoneFormat)
    outputDateShortFormatSdf.timeZone = timezone ?: TimeZone.getDefault()
    return outputDateShortFormatSdf.format(this)
}
