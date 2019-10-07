package org.rfcx.ranger.util

import android.content.Context
import org.rfcx.ranger.R
import java.util.*

private const val SECOND: Long = 1000
private const val MINUTE = 60 * SECOND
private const val HOUR = 60 * MINUTE
private const val DAY = 24 * HOUR
private const val WEEK = 7 * DAY

fun Context?.getPastedTimeFormat(d: Date): String {
	
	if (this == null) return "-"
	val long = d.millisecondsSince()
	val minAgo = MINUTE
	val hourAgo = HOUR
	val dayAgo = DAY
	val weekAgo = WEEK
	
	return when {
		long < minAgo -> this.getString(R.string.report_time_second)
		long < hourAgo -> {
			val diffMinute = (long / MINUTE).toInt()
			this.getString(R.string.report_minutes_format, diffMinute)
		}
		long < dayAgo -> {
			val diffHour = (long / HOUR).toInt()
			this.getString(R.string.report_hr_format, diffHour)
		}
		long < weekAgo -> {
			val diffDay = (long / DAY).toInt()
			this.getString(R.string.report_day_format, diffDay)
		}
		else -> {
			val diffWeek = (long / WEEK).toInt()
			this.getString(R.string.report_week_format, diffWeek)
		}
	}
}

fun <T> List<T>.replace(newValue: T, block: (T) -> Boolean): List<T> {
	return map {
		if (block(it)) newValue else it
	}
}
