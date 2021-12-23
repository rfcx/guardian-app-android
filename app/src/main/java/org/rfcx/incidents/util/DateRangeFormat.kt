package org.rfcx.incidents.util

import android.annotation.SuppressLint
import android.content.Context
import android.text.format.DateUtils
import org.rfcx.incidents.R
import java.text.SimpleDateFormat
import java.util.*


class DateRangeFormat {
	
	private val timeFormat = "HH:mm"
	private val dateShortFormat = "dd MMM"
	
	
	fun dateRangeFormat(context: Context, fromUtc: Date, toUtc: Date, timezone: TimeZone? = null): String {
		var text = ""
		
		if (isToday(fromUtc.time)) {
			if (isToday(toUtc.time)) text = context.getString(R.string.is_today, fromUtc.toTimeTextString(timezone), toUtc.toTimeTextString(timezone))
		} else if (isYesterday(fromUtc.time)) {
			text = if (isToday(toUtc.time)) {
				context.getString(R.string.is_yesterday_today, fromUtc.toTimeTextString(timezone), toUtc.toTimeTextString(timezone))
			} else {
				context.getString(R.string.is_yesterday, fromUtc.toTimeTextString(timezone), toUtc.toTimeTextString(timezone))
			}
		} else {
			text = if (isToday(toUtc.time)) {
				context.getString(R.string.is_other_today, fromUtc.toShortDateString(timezone), toUtc.toTimeTextString(timezone))
			} else if (isYesterday(toUtc.time)) {
				context.getString(R.string.is_other_yesterday, fromUtc.toShortDateString(timezone), toUtc.toTimeTextString(timezone))
			} else if (isSameDate(fromUtc, toUtc)) {
				context.getString(R.string.is_same_date, fromUtc.toShortDateString(timezone), fromUtc.toTimeTextString(timezone), toUtc.toTimeTextString(timezone))
			} else {
				context.getString(R.string.is_other_other, fromUtc.toShortDateString(timezone), toUtc.toShortDateString(timezone))
			}
		}
		return text
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
}
