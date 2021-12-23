package org.rfcx.incidents.util

import android.content.Context
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import java.util.*

class DateRangeFormatTest {
	private lateinit var context: Context
	
	@Before
	fun setup() {
		context = InstrumentationRegistry.getInstrumentation().targetContext
	}
	
	/* ------------- 1.today - today => Today, X-Y -----------------------*/
	@Test
	fun caseStartTodayAndEndToday() {
		// today - today => Today, X-Y
		
		val startAt = Date(Date.UTC(121, 11, 23, 2, 31, 0))
		val endAt = Date(Date.UTC(121, 11, 23, 7, 15, 0))
		
		Assert.assertEquals(DateRangeFormat().dateRangeFormat(context, startAt, endAt), "Today, 09:31 - 14:15")
	}
	
	@Test
	fun caseStartTodayAndEndTodayWithTimeZone() {
		// today - today => Today, X-Y
		
		val startAt = Date(Date.UTC(121, 11, 23, 6, 31, 0))
		val endAt = Date(Date.UTC(121, 11, 23, 7, 15, 0))
		
		val timeZone: TimeZone = TimeZone.getTimeZone("America/New_York") // UTC -5
		
		Assert.assertEquals(DateRangeFormat().dateRangeFormat(context, startAt, endAt, timeZone), "Today, 01:31 - 02:15")
	}
	
	/* ------------- 2.yesterday - today => Yesterday X - Today Y -----------------------*/
	@Test
	fun caseStartYesterdayAndEndToday() {
		// yesterday - today => Yesterday X - Today Y
		val startAt = Date(Date.UTC(121, 11, 22, 10, 31, 0))
		val endAt = Date(Date.UTC(121, 11, 23, 7, 25, 0))
		
		Assert.assertEquals(DateRangeFormat().dateRangeFormat(context, startAt, endAt), "Yesterday 17:31 - Today 14:25")
	}
	
	@Test
	fun caseStartYesterdayAndEndTodayWithTimeZone() {
		// yesterday - today => Yesterday X - Today Y
		val startAt = Date(Date.UTC(121, 11, 22, 10, 31, 0))
		val endAt = Date(Date.UTC(121, 11, 23, 7, 25, 0))
		
		val timeZone: TimeZone = TimeZone.getTimeZone("America/New_York") // UTC -5
		
		Assert.assertEquals(DateRangeFormat().dateRangeFormat(context, startAt, endAt, timeZone), "Yesterday 05:31 - Today 02:25")
	}
	
	/* ------------- 3.yesterday - yesterday => Yesterday, X-Y -----------------------*/
	@Test
	fun caseStartYesterdayAndEndYesterday() {
		// yesterday - yesterday => Yesterday, X-Y
		
		val startAt = Date(Date.UTC(121, 11, 22, 1, 31, 0))
		val endAt = Date(Date.UTC(121, 11, 22, 7, 45, 0))
		
		Assert.assertEquals(DateRangeFormat().dateRangeFormat(context, startAt, endAt), "Yesterday, 08:31 - 14:45")
	}
	
	@Test
	fun caseStartYesterdayAndEndYesterdayWithTimeZone() {
		// yesterday - yesterday => Yesterday, X-Y
		
		val startAt = Date(Date.UTC(121, 11, 22, 10, 31, 0))
		val endAt = Date(Date.UTC(121, 11, 22, 12, 45, 0))
		
		val timeZone: TimeZone = TimeZone.getTimeZone("Asia/Tbilisi") // UTC +4
		
		Assert.assertEquals(DateRangeFormat().dateRangeFormat(context, startAt, endAt, timeZone), "Yesterday, 14:31 - 16:45")
	}
	
	/* ------------- 4.other - today => 10 Dec - Today, Y -----------------------*/
	@Test
	fun caseStartOtherAndEndToday() {
		// other - today => 10 Dec - Today, Y
		
		val startAt = Date(Date.UTC(121, 11, 8, 1, 31, 0))
		val endAt = Date(Date.UTC(121, 11, 23, 7, 45, 0))
		
		Assert.assertEquals(DateRangeFormat().dateRangeFormat(context, startAt, endAt), "08 Dec - Today, 14:45")
	}
	
	@Test
	fun caseStartOtherAndEndTodayWithTimeZone() {
		// other - today => 10 Dec - Today, Y
		
		val startAt = Date(Date.UTC(121, 11, 8, 1, 31, 0))
		val endAt = Date(Date.UTC(121, 11, 23, 7, 45, 0))
		
		val timeZone: TimeZone = TimeZone.getTimeZone("Pacific/Tahiti") // UTC -10
		
		Assert.assertEquals(DateRangeFormat().dateRangeFormat(context, startAt, endAt, timeZone), "07 Dec - Today, 21:45")
	}
	
	/* ------------- 5.other - yesterday => 10 Dec - Yesterday, Y -----------------------*/
	@Test
	fun caseStartOtherAndEndYesterday() {
		// other - yesterday => 10 Dec - Yesterday, Y
		
		val startAt = Date(Date.UTC(121, 11, 10, 10, 31, 0))
		val endAt = Date(Date.UTC(121, 11, 22, 12, 45, 0))
		
		Assert.assertEquals(DateRangeFormat().dateRangeFormat(context, startAt, endAt), "10 Dec - Yesterday, 19:45")
	}
	
	@Test
	fun caseStartOtherAndEndYesterdayWithTimeZone() {
		// other - yesterday => 10 Dec - Yesterday, Y
		
		val startAt = Date(Date.UTC(121, 11, 10, 10, 31, 0))
		val endAt = Date(Date.UTC(121, 11, 21, 22, 45, 0))
		
		val timeZone: TimeZone = TimeZone.getTimeZone("Asia/Beirut") // UTC +2
		
		Assert.assertEquals(DateRangeFormat().dateRangeFormat(context, startAt, endAt, timeZone), "10 Dec - Yesterday, 00:45")
	}
	
	/* ------------- 6.other - other => 11 Dec - 12 Dec -----------------------*/
	@Test
	fun caseStartOtherAndEndOther() {
		// other - other => 11 Dec - 12 Dec
		
		val startAt = Date(Date.UTC(121, 11, 10, 10, 31, 0))
		val endAt = Date(Date.UTC(121, 11, 15, 15, 45, 0))
		
		Assert.assertEquals(DateRangeFormat().dateRangeFormat(context, startAt, endAt), "10 Dec - 15 Dec")
	}
	
	@Test
	fun caseStartOtherAndEndOtherWithTimeZone() {
		// other - other => 11 Dec - 12 Dec
		
		val startAt = Date(Date.UTC(121, 11, 10, 10, 31, 0))
		val endAt = Date(Date.UTC(121, 11, 15, 17, 45, 0))
		
		val timeZone: TimeZone = TimeZone.getTimeZone("Asia/Beirut") // UTC +2
		
		Assert.assertEquals(DateRangeFormat().dateRangeFormat(context, startAt, endAt, timeZone), "10 Dec - 15 Dec")
	}
	
	/* ------------- 7.other - other => 11 Dec,  X-Y -----------------------*/
	@Test
	fun caseStartOtherAndEndOtherBySameDay() {
		// other - other => 11 Dec,  X-Y
		
		val startAt = Date(Date.UTC(121, 11, 11, 20, 31, 0))
		val endAt = Date(Date.UTC(121, 11, 12, 1, 45, 0))
		
		Assert.assertEquals(DateRangeFormat().dateRangeFormat(context, startAt, endAt), "12 Dec, 03:31 - 08:45")
	}
	
	@Test
	fun caseStartOtherAndEndOtherBySameDayWithTimeZone() {
		// other - other => 11 Dec,  X-Y
		
		val startAt = Date(Date.UTC(121, 11, 11, 1, 31, 0))
		val endAt = Date(Date.UTC(121, 11, 11, 2, 45, 0))
		
		val timeZone: TimeZone = TimeZone.getTimeZone("Asia/Beirut") // UTC +2
		
		Assert.assertEquals(DateRangeFormat().dateRangeFormat(context, startAt, endAt, timeZone), "11 Dec, 03:31 - 04:45")
	}
}
