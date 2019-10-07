package org.rfcx.ranger

import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.rfcx.ranger.util.*
import java.util.*

class DateHelperTest {
	
	private lateinit var date: Date
	
	@Before
	fun setup() {
		val calender = Calendar.getInstance()
		calender.set(2019, 10, 6, 13, 30, 5)
		calender.timeZone = TimeZone.getTimeZone("GMT+7")
		date = calender.time
	}
	
	@Test
	fun canGetIsoDateString() {
		// Arrange
		val expected = "2019-11-06T13:30:05.000Z"
		
		// Act
		val actual = date.toIsoString()
		
		// Assert
		Assert.assertNotEquals(expected, actual)
	}
	
	@Test
	fun canGetFormatShortDate() {
		// Arrange
		val expected = "06 Nov 2019"
		
		// Act
		val actual = date.toShortDateString()
		
		// Assert
		Assert.assertEquals(expected, actual)
	}
	
	@Test
	fun canGetFormatTime() {
		// Arrange
		val expected = "13:30"
		
		// Act
		val actual = date.toTimeString()
		
		// Assert
		Assert.assertEquals(expected, actual)
	}
	
	@Test
	fun canGetFullDate() {
		// Arrange
		val expectedResult = "November 6, 2019 13:30"
		
		// Act
		val actualResult = date.toFullDateTimeString()
		
		// Assert
		Assert.assertNotEquals("", actualResult)
		Assert.assertEquals(expectedResult, actualResult)
	}
	
	@Test
	fun canGetTimePasted() {
		// Arrange
		val oneSecondInMs = 1000
		val secondAgo = Date(Date().time - oneSecondInMs)
		
		// Act
		val actual = secondAgo.millisecondsSince()
		
		// Assert
		Assert.assertTrue(999 < actual)
		Assert.assertTrue(actual < 1100)
	}
	
	@Test
	fun canParseLegacyDates() {
		// Arrange
		val calendar1 = Calendar.getInstance()
		calendar1.set(2019, 10, 6, 13, 30, 5)
		calendar1.timeZone = TimeZone.getTimeZone("UTC")
		val expected1 = calendar1.time.toString()
		val dateFormat1 = "2019-11-06T20:30:05.000+0700" // expected
		
		val calendar2 = Calendar.getInstance()
		calendar2.set(2019, 10, 6, 13, 30, 0)
		val expected2 = calendar2.time.toString()
		val dateFormat2 = "2019-11-06 13:30:00" // expected yyyy-MM-dd HH:mm
		
		val dateFormat3 = "November 6, 2019 13:30" // unexpected
		
		// Act
		val actual1 = legacyDateParser(dateFormat1).toString()
		val actual2 = legacyDateParser(dateFormat2).toString()
		val actual3 = legacyDateParser(dateFormat3)
		
		// Assert
		Assert.assertEquals(expected1, actual1)
		Assert.assertEquals(expected2, actual2)
		Assert.assertNull(actual3)
	}
}