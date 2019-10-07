package org.rfcx.ranger.util

import android.util.Log
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*


object DateHelper {
	
	private const val dateTimeFormat = "yyyy-MM-dd HH:mm"
	private const val dateTimeSecondFormat = "yyyy-MM-dd HH:mm:ss"
	private const val shortDateFormat = "dd MMM yyyy"
	private const val timeFormat = "HH:mm"
	private const val standardDateFormat = "MMMM d, yyyy HH:mm"
	
	private const val SECOND: Long = 1000
	const val MINUTE = 60 * SECOND
	const val HOUR = 60 * MINUTE
	const val DAY = 24 * HOUR
	const val WEEK = 7 * DAY
	
	private val inputUtcSdf by lazy {
		val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
		sdf.timeZone = TimeZone.getTimeZone("UTC")
		sdf
	}
	
	private val inputSdf by lazy {
		val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZZZZZ", Locale.US)
		sdf
	}
	
	private val inputDateTimeSdf by lazy {
		val sdf = SimpleDateFormat(dateTimeFormat, Locale.US)
		sdf
	}
	
	private val outputDateTimeSecondSdf by lazy {
		val sdf = SimpleDateFormat(dateTimeSecondFormat, Locale.getDefault())
		sdf.timeZone = TimeZone.getDefault()
		sdf
	}
	
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
	
	private val inputFormats = arrayListOf(
			inputUtcSdf,
			inputSdf,
			inputDateTimeSdf)
	
	fun getIsoTime(d: Date = Date()): String {
		// pattern 2008-09-15T15:53:00+05:00
		return try {
			return inputSdf.format(d)
		} catch (e: Exception) {
			e.printStackTrace()
			""
		}
	}
	
	fun formatShortDate(d: Date?): String {
		return if (d != null)
			outputShortDateSdf.format(d)
		else
			""
	}
	
	fun formatDateTimeSecond(d: Date?): String {
		return if (d != null)
			outputDateTimeSecondSdf.format(d)
		else
			""
	}
	
	fun formatTime(d: Date?): String {
		return if (d != null)
			outputTimeSdf.format(d)
		else
			""
	}
	
	fun formatFullDate(d: Date?): String {
		return if (d != null)
			outputStandardDateSdf.format(d)
		else
			""
	}
	
	fun getTimePasted(d: Date): Long {
		val currentDateTime = Date()
		return currentDateTime.time - d.time
	}
	
	fun parseToDate(input: String?): Date? {
		input ?: return null
		
		inputFormats.forEach {
			try {
				val date = it.parse(input)
				Log.i("DateHelper", "date -> $date")
				return date
			} catch (e: ParseException) {
				Log.i("DateHelper", "parse fail with ${it.toPattern()}")
			}
		}
		return null // not found format matching
	}
}
