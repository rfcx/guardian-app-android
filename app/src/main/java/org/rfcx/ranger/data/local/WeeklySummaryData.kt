package org.rfcx.ranger.data.local

import org.rfcx.ranger.util.Preferences
import java.util.*

class WeeklySummaryData(private val preferences: Preferences) {
	
	init {
		cleanSummaryIfNeed()
	}
	
	fun adJustReviewCount() {
		var lastedCount: Int = preferences.getInt(REVIEW_COUNT, 0)
		lastedCount += 1
		preferences.putInt(REVIEW_COUNT, lastedCount)
	}
	
	fun getReviewCount(): Int = preferences.getInt(REVIEW_COUNT, 0)
	
	fun adJustReportSubmitCount() {
		var lastedCount: Int = preferences.getInt(REPORT_COUNT, 0)
		lastedCount += 1
		preferences.putInt(REPORT_COUNT, lastedCount)
	}
	
	fun adJustRportSubmitCount(total: Int) {
		preferences.putInt(REPORT_COUNT, total)
	}
	
	fun getReportSubmitCount(): Int = preferences.getInt(REPORT_COUNT, 0)
	
	/**
	 * return onDuty time in Minute
	 */
	fun getOnDutyTimeMinute(): Long {
		
		val lastOnDutyTime = preferences.getLong(ON_DUTY, 0L)
		val lastDutyOpenTime = preferences.getLong(ON_DUTY_LAST_OPEN, 0L)
		
		return if (lastDutyOpenTime != 0L) {
			val currentTime = System.currentTimeMillis()
			val difTime = currentTime - lastDutyOpenTime
			val onDutyNow = difTime / MILLI_SECS_PER_MINUTE
			lastOnDutyTime + onDutyNow
		} else {
			preferences.getLong(ON_DUTY, 0L)
		}
	}
	
	private fun adjustOnDuty(minutes: Int) {
		var lastedDuty: Long = preferences.getLong(ON_DUTY, 0L)
		lastedDuty += minutes
		preferences.putLong(ON_DUTY, lastedDuty)
	}
	
	
	fun startDutyTracking() {
		if (preferences.getLong(ON_DUTY_LAST_OPEN, 0L) == 0L) {
			preferences.putLong(ON_DUTY_LAST_OPEN, System.currentTimeMillis())
		}
	}
	
	fun stopDutyTracking() {
		val lastOpen: Long = preferences.getLong(ON_DUTY_LAST_OPEN, 0L)
		val stopTime = System.currentTimeMillis()
		preferences.putLong(ON_DUTY_LAST_OPEN, 0L)
		
		if (lastOpen != 0L && stopTime > lastOpen) {
			val onDutyMinute = ((stopTime - lastOpen) / MILLI_SECS_PER_MINUTE).toInt()
			adjustOnDuty(onDutyMinute)
		}
		
	}
	
	/**
	 * do delete summary every monday
	 */
	private fun cleanSummaryIfNeed() {
		
		val currentCalendar = Calendar.getInstance()
		currentCalendar.set(Calendar.HOUR_OF_DAY, 0)
		currentCalendar.set(Calendar.MINUTE, 0)
		currentCalendar.set(Calendar.SECOND, 0)
		currentCalendar.set(Calendar.MILLISECOND, 0)
		
		val mondayCalendar = Calendar.getInstance()
		mondayCalendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
		mondayCalendar.set(Calendar.HOUR_OF_DAY, 0)
		mondayCalendar.set(Calendar.MINUTE, 0)
		mondayCalendar.set(Calendar.SECOND, 0)
		mondayCalendar.set(Calendar.MILLISECOND, 0)
		val mondayOfThisWeek = mondayCalendar.time
		
		val lastMondayCleanUp = preferences.getDate(LAST_CLEAN_DATA_MONDAY)
		
		var lastCleanUpFromNow = 0
		if (lastMondayCleanUp != null) {
			lastCleanUpFromNow = ((currentCalendar.time.time - lastMondayCleanUp.time) / MILLI_SECS_PER_DAY).toInt()
		}
		
		if ((mondayOfThisWeek == currentCalendar.time && mondayOfThisWeek != lastMondayCleanUp)
				|| lastCleanUpFromNow >= 7) {
			cleanSummaryData()
		}
		
	}
	
	private fun cleanSummaryData() {
		
		preferences.remove(REVIEW_COUNT)
		preferences.remove(REPORT_COUNT)
		preferences.remove(ON_DUTY)
		
		val mondayCalendar = Calendar.getInstance()
		mondayCalendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
		mondayCalendar.set(Calendar.HOUR_OF_DAY, 0)
		mondayCalendar.set(Calendar.MINUTE, 0)
		mondayCalendar.set(Calendar.SECOND, 0)
		mondayCalendar.set(Calendar.MILLISECOND, 0)
		val mondayOfThisWeek = mondayCalendar.time
		
		// save on duty open time to monday if it's on
		val lastDutyOpenTime = preferences.getLong(ON_DUTY_LAST_OPEN, 0L)
		if (lastDutyOpenTime != 0L) {
			preferences.putLong(ON_DUTY_LAST_OPEN, mondayOfThisWeek.time)
		}
		
		preferences.putDate(LAST_CLEAN_DATA_MONDAY, mondayOfThisWeek)
	}
	
	
	companion object {
		private const val REVIEW_COUNT = Preferences.PREFIX + "REVIEW_COUNT"
		private const val REPORT_COUNT = Preferences.PREFIX + "REPORT_COUNT"
		private const val ON_DUTY = Preferences.PREFIX + "ON_DUTY_TIME"
		private const val ON_DUTY_LAST_OPEN = Preferences.PREFIX + "ON_DUTY_LAST_OPEN"
		
		private const val LAST_CLEAN_DATA_MONDAY = Preferences.PREFIX + "LAST_CLEAN_DATA_MONDAY"
		private const val MILLI_SECS_PER_DAY = (24 * 60 * 60 * 1000).toLong()
		private const val MILLI_SECS_PER_MINUTE = (60 * 1000).toLong()
	}
}