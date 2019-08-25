package org.rfcx.ranger.util

import org.rfcx.ranger.entity.event.Event
import java.text.SimpleDateFormat
import java.util.*


/**
 *
 * TODO refactor this class
 */
object DateHelper {
	
	private const val inputFormat = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"
	private const val dateTimeFormat = "yyyy-MM-dd HH:mm"
	const val dateTimeFormatSecond = "yyyy-MM-dd HH:mm:ss"
	private const val dateFormat = "yyyy-MM-dd"
	const val timeFormat = "HH:mm"
	
	private const val oneDayMs = 24L * 3600000L

	const val SECOND: Long = 1000
	const val MINUTE = 60 * SECOND
	const val HOUR = 60 * MINUTE
	const val DAY = 24 * HOUR
	const val WEEK = 7 * DAY
	const val MONTH = 4 * WEEK

	private val inputSdf by lazy {
		val sdf = SimpleDateFormat(inputFormat, Locale.US)
		sdf.timeZone = TimeZone.getTimeZone("UTC")
		sdf
	}
	
	private val outputDateTimeSdf by lazy {
		val sdf = SimpleDateFormat(dateTimeFormat, Locale.getDefault())
		sdf.timeZone = TimeZone.getDefault()
		sdf
	}
	
	private val outputDateTimeSecondSdf by lazy {
		val sdf = SimpleDateFormat(dateTimeFormatSecond, Locale.getDefault())
		sdf.timeZone = TimeZone.getDefault()
		sdf
	}
	
	private val outputDateSdf by lazy {
		val sdf = SimpleDateFormat(dateFormat, Locale.getDefault())
		sdf.timeZone = TimeZone.getDefault()
		sdf
	}
	
	private val outputTimeSdf by lazy {
		val sdf = SimpleDateFormat(timeFormat, Locale.getDefault())
		sdf.timeZone = TimeZone.getDefault()
		sdf
	}
	
	fun getDateTime(input: String?): Date? {
		return try {
			inputSdf.parse(input)
		} catch (e: java.lang.Exception) {
			e.printStackTrace()
			null
		}
	}
	
	
	fun getMessageDateTime(input: String): String {
		return try {
			val d: Date = inputSdf.parse(input)
			outputDateTimeSdf.format(d)
		} catch (e: Exception) {
			""
		}
	}
	
	fun getEventTime(event: Event): String {
		val d1: Date
		try {
			d1 = inputSdf.parse(event.beginsAt)
		} catch (e: Exception) {
			return ""
		}
		if (d1.before(Date(System.currentTimeMillis() - oneDayMs))) {
			return outputDateTimeSdf.format(d1)
		}
		return outputTimeSdf.format(d1)
	}
	
	fun getEventDate(input: String?): String {
		return try {
			val d: Date = inputSdf.parse(input)
			outputDateSdf.format(d)
		} catch (e: Exception) {
			""
		}
	}
	
	
	fun getIsoTime(): String {
		// pattern 2008-09-15T15:53:00+05:00
		return try {
			val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZZZZZ", Locale.getDefault())
			val d = Date(System.currentTimeMillis())
			return sdf.format(d)
		} catch (e: Exception) {
			e.printStackTrace()
			""
		}
	}
	
	fun parse(isoTime: String): String {
		return try {
			val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZZZZZ", Locale.getDefault())
			val d = sdf.parse(isoTime)
			return outputDateTimeSecondSdf.format(d)
		} catch (e: Exception) {
			e.printStackTrace()
			""
		}
	}
	
	fun parse(isoTime: String, outputFormat: String): String {
		return try {
			val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZZZZZ", Locale.getDefault())
			val d = sdf.parse(isoTime)
			val outputSdf = SimpleDateFormat(outputFormat, Locale.getDefault())
			return outputSdf.format(d)
		} catch (e: Exception) {
			e.printStackTrace()
			""
		}
	}
	
	fun getCurrent(destinationFormat: String): String {
		return try {
			val sdf = SimpleDateFormat(destinationFormat, Locale.getDefault())
			sdf.format(Calendar.getInstance().time)
		} catch (e: Exception) {
			e.printStackTrace()
			""
		}
	}
	
	/**
	 * @param utcTime
	 * return diff between current time and param 'utcTime'
	 * return diff time in millisecond
	 */
	fun getTimePasted(utcTime: String): Long {
		
		return try {
			val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZZZZZ", Locale.getDefault())
			val inputDateTime = sdf.parse(utcTime)
			val currentDateTime = Date()
			return currentDateTime.time - inputDateTime.time
		} catch (e: Exception) {
			e.printStackTrace()
			0
		}
	}
	
}
