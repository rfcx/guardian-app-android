package org.rfcx.ranger.util

import android.content.Context
import org.rfcx.ranger.R
import org.rfcx.ranger.util.DateHelper.DAY
import org.rfcx.ranger.util.DateHelper.HOUR
import org.rfcx.ranger.util.DateHelper.MINUTE
import org.rfcx.ranger.util.DateHelper.WEEK
import java.util.*


fun Context?.getPastedTimeFormat(long: Long): String {
	
	if (this == null) return "-"
	
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