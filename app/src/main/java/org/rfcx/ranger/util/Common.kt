package org.rfcx.ranger.util

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.Drawable
import android.text.format.DateUtils
import android.text.format.DateUtils.MINUTE_IN_MILLIS
import android.widget.ImageView
import androidx.core.content.ContextCompat
import org.joda.time.Duration
import org.rfcx.ranger.R
import java.util.*

private const val SECOND: Long = 1000
private const val MINUTE = 60 * SECOND
private const val HOUR = 60 * MINUTE
private const val DAY = 24 * HOUR
private const val WEEK = 7 * DAY

fun Date.toTimeSinceString(context: Context?): String {
	
	if (context == null) return "-"
	val long = this.millisecondsSince()
	val minAgo = MINUTE
	val hourAgo = HOUR
	val dayAgo = DAY
	val weekAgo = WEEK
	
	return when {
		long < minAgo -> context.getString(R.string.report_time_second)
		long < hourAgo -> {
			val diffMinute = (long / MINUTE).toInt()
			context.getString(R.string.report_minutes_format, diffMinute)
		}
		long < dayAgo -> {
			val diffHour = (long / HOUR).toInt()
			context.getString(R.string.report_hr_format, diffHour)
		}
		long < weekAgo -> {
			val diffDay = (long / DAY).toInt()
			context.getString(R.string.report_day_format, diffDay)
		}
		else -> {
			val diffWeek = (long / WEEK).toInt()
			context.getString(R.string.report_week_format, diffWeek)
		}
	}
}

fun Date.toTimeSinceStringAlternative(context: Context): String {
	val diff = Duration(this.time, Date().time).standardHours
	return if (this.isToday()) {
		this.toTimeString()
	} else if (diff < 48) {
		"${context.getString(R.string.yesterday)} ${this.toTimeString()}"
	} else {
		this.toFullDateTimeString()
	}
}

@SuppressLint("SimpleDateFormat")
fun Date.toTimeSinceStringAlternativeTimeAgo(context: Context): String {
	val niceDateStr = DateUtils.getRelativeTimeSpanString(this.time, Calendar.getInstance().timeInMillis, MINUTE_IN_MILLIS)
	
	return if (niceDateStr.toString() == "0 minutes ago") {
		context.getString(R.string.report_time_second)
	} else if (niceDateStr.toString() == "Yesterday") {
		"${context.getString(R.string.yesterday)} ${this.toTimeString()}"
	} else if (!niceDateStr.toString().contains("ago")) {
		this.toDateTimeString()
	} else if (niceDateStr.toString().contains("days ago")) {
		this.toDateTimeString()
	} else {
		niceDateStr.toString()
	}
}

fun <T> List<T>.replace(newValue: T, block: (T) -> Boolean): List<T> {
	return map {
		if (block(it)) newValue else it
	}
}

fun Context.getImage(res: Int): Drawable? {
	return ContextCompat.getDrawable(this, res)
}

fun Context.getBackgroundColor(res: Int): Int {
	return ContextCompat.getColor(this, res)
}

fun String.isValidEmail() =
		this.isNotEmpty() && android.util.Patterns.EMAIL_ADDRESS.matcher(this).matches()

fun Double.setFormatLabel(): String {
	return if (this >= 1000) "${String.format("%.1f", this / 1000)}km" else "${String.format("%.0f", this)}m"
}

fun Float.setFormatLabel(): String {
	return if (this >= 1000) "${String.format("%.1f", this / 1000)}km" else "${String.format("%.0f", this)}m"
}

fun ImageView.setDrawableImage(context: Context, id: Int) {
	this.setImageDrawable(ContextCompat.getDrawable(context, id))
}

fun Calendar.getDay(): Int = this.get(Calendar.DAY_OF_MONTH)
fun Calendar.getMonth(): Int = this.get(Calendar.MONTH)
fun Calendar.getYear(): Int = this.get(Calendar.YEAR)
