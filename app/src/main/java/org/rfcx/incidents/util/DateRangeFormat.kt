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
	
	
	fun dateRangeFormat(context: Context, fromUtc: String, toUtc: String, timezone: TimeZone? = null): String {
		var text = ""
		val startAt = fromUtc.toIsoString()
		val endAt = toUtc.toIsoString()
		
		if (isToday(startAt.time)) {
			if (isToday(endAt.time)) text = context.getString(R.string.is_today, startAt.toTimeTextString(timezone), endAt.toTimeTextString(timezone))
		} else if (isYesterday(startAt.time)) {
			text = if (isToday(endAt.time)) {
				context.getString(R.string.is_yesterday_today, startAt.toTimeTextString(timezone), endAt.toTimeTextString(timezone))
			} else {
				context.getString(R.string.is_yesterday, startAt.toTimeTextString(timezone), endAt.toTimeTextString(timezone))
			}
		} else {
			text = if (isToday(endAt.time)) {
				context.getString(R.string.is_other_today, startAt.toShortDateString(timezone), endAt.toTimeTextString(timezone))
			} else if (isYesterday(endAt.time)) {
				context.getString(R.string.is_other_yesterday, startAt.toShortDateString(timezone), endAt.toTimeTextString(timezone))
			} else if (isSameDate(startAt, endAt)) {
				context.getString(R.string.is_same_date, startAt.toShortDateString(timezone), startAt.toTimeTextString(timezone), endAt.toTimeTextString(timezone))
			} else {
				context.getString(R.string.is_other_other, startAt.toShortDateString(timezone), endAt.toShortDateString(timezone))
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
