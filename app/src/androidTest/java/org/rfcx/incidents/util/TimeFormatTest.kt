package org.rfcx.incidents.util

import android.content.Context
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import java.util.*

class TimeFormatTest {
	private lateinit var context: Context
	
	@Before
	fun setup() {
		context = InstrumentationRegistry.getInstrumentation().targetContext
	}
	
	@Test
	fun caseStartTodayAndEndToday() {
		// today - today => Today, X-Y
		
		val startAt = Date(121, 11, 22, 10, 31)
		val endAt = Date(121, 11, 22, 17, 45)
		
		Assert.assertEquals(timeFormat(context, startAt, endAt), "Today, 10:31 - 17:45")
	}
	
	@Test
	fun caseStartYesterdayAndEndToday() {
		// yesterday - today => Yesterday X - Today Y
		
		val startAt = Date(121, 11, 21, 10, 31)
		val endAt = Date(121, 11, 22, 17, 45)
		
		Assert.assertEquals(timeFormat(context, startAt, endAt), "Yesterday 10:31 - Today 17:45")
	}
	
	@Test
	fun caseStartYesterdayAndEndYesterday() {
		// yesterday - yesterday => Yesterday, X-Y
		
		val startAt = Date(121, 11, 21, 10, 31)
		val endAt = Date(121, 11, 21, 17, 45)
		
		Assert.assertEquals(timeFormat(context, startAt, endAt), "Yesterday, 10:31 - 17:45")
	}
	
	@Test
	fun caseStartOtherAndEndToday() {
		// other - today => 10 Dec - Today, Y
		
		val startAt = Date(121, 11, 10, 10, 31)
		val endAt = Date(121, 11, 22, 17, 45)
		
		Assert.assertEquals(timeFormat(context, startAt, endAt), "10 Dec - Today, 17:45")
	}
	
	@Test
	fun caseStartOtherAndEndYesterday() {
		// other - yesterday => 10 Dec - Yesterday, Y
		
		val startAt = Date(121, 11, 10, 10, 31)
		val endAt = Date(121, 11, 21, 17, 45)
		
		Assert.assertEquals(timeFormat(context, startAt, endAt), "10 Dec - Yesterday, 17:45")
	}
	
	@Test
	fun caseStartOtherAndEndOther() {
		// other - other => 11 Dec - 12 Dec
		
		val startAt = Date(121, 11, 10, 10, 31)
		val endAt = Date(121, 11, 15, 17, 45)
		
		Assert.assertEquals(timeFormat(context, startAt, endAt), "10 Dec - 15 Dec")
	}
	
	@Test
	fun caseStartOtherAndEndOtherBySameDay() {
		// other - other => 11 Dec,  X-Y
		
		val startAt = Date(121, 11, 10, 10, 31)
		val endAt = Date(121, 11, 10, 17, 45)
		
		Assert.assertEquals(timeFormat(context, startAt, endAt), "10 Dec, 10:31 - 17:45")
	}
}
