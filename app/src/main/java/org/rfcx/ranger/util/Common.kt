package org.rfcx.ranger.util

import android.content.Context
import org.rfcx.ranger.R


fun Context?.getPastedTimeFormat(long: Long): String {
	
	if (this == null) return "-"
	val diffDate: Int = (long / (24L * 3600000L)).toInt()
	return if (diffDate < 1) {
		val diffHour = (long / (1000 * 60 *60)).toInt()
		this.getString(R.string.report_hr_format, diffHour)
	} else {
		this.getString(R.string.report_day_format, diffDate)
	}
}