package org.rfcx.ranger.util

import org.rfcx.ranger.entity.event.Event
import java.text.SimpleDateFormat
import java.util.*


/**
 *
 * TODO refactor this class
 */
object DateHelper {
	
	private const val dateTimeFormat = "yyyy-MM-dd HH:mm"
	private const val dateTimeSecondFormat = "yyyy-MM-dd HH:mm:ss"
	private const val dateFormat = "yyyy-MM-dd"
	private const val shortDateFormat = "dd MMM yyyy"
	const val timeFormat = "HH:mm" // TODO should be private
	
	private const val oneDayMs = 24L * 3600000L
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
	
	private val outputDateTimeSdf by lazy {
		val sdf = SimpleDateFormat(dateTimeFormat, Locale.getDefault())
		sdf.timeZone = TimeZone.getDefault()
		sdf
	}
	
	private val outputDateTimeSecondSdf by lazy {
		val sdf = SimpleDateFormat(dateTimeSecondFormat, Locale.getDefault())
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
	
	private val outputShortDateSdf by lazy {
		val sdf = SimpleDateFormat(shortDateFormat, Locale.getDefault())
		sdf.timeZone = TimeZone.getDefault()
		sdf
	}
	
	fun getDateTime(input: String?): Date? {
		if (input == null) return null
		val isUtc = !input.contains('+')
		return try {
			if (isUtc) inputUtcSdf.parse(input) else inputSdf.parse(input) // TODO should be 2 different functions
		} catch (e: java.lang.Exception) {
			e.printStackTrace()
			null
		}
	}
	
	
	fun getMessageDateTime(input: String): String {
		return try {
			val d: Date = inputUtcSdf.parse(input)
			outputDateTimeSdf.format(d)
		} catch (e: Exception) {
			""
		}
	}
	
	fun getEventTime(event: Event): String {
		val d1: Date
		try {
			d1 = inputUtcSdf.parse(event.beginsAt)
		} catch (e: Exception) {
			return ""
		}
		if (d1.before(Date(System.currentTimeMillis() - oneDayMs))) {
			return outputDateTimeSdf.format(d1)
		}
		return outputTimeSdf.format(d1)
	}
	
	
	fun getIsoTime(): String {
		// pattern 2008-09-15T15:53:00+05:00
		return try {
			return inputSdf.format(Date())
		} catch (e: Exception) {
			e.printStackTrace()
			""
		}
	}
	
	fun parse(isoTime: String): String {
		return try {
			val d = inputSdf.parse(isoTime)
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
	
	fun formatShortDate(isoTime: String): String {
		val d = getDateTime(isoTime)
		if (d != null)
			return outputShortDateSdf.format(d)
		else
			return ""
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
