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
		date = calender.time
	}
	
	@Test
	fun canGetIsoDateString() {
		// Arrange
		val date = Date(2019, 11, 6, 13, 40, 5)
		val expected = "2019-11-06T13:30:05.000Z"
		
		// Act
		val actual = date.toIsoString()
		
		// Assert
		Assert.assertNotEquals(expected, actual)
	}
	
	@Test
	fun canGetFormatShotDate() {
		// Arrange
		val expectedResult = "06 Nov 2019"
		
		// Act
		val actualResult = DateHelper.formatShortDate(date)
		
		// Assert
		Assert.assertNotEquals("", actualResult)
		Assert.assertEquals(expectedResult, actualResult)
	}
	
	@Test
	fun canGetFormatTime() {
		// Arrange
		val expectedResult = "13:30"
		
		// Act
		val actualResult = date.formatTime()
		
		// Assert
		Assert.assertNotEquals("", actualResult)
		Assert.assertEquals(expectedResult, actualResult)
	}
	
	@Test
	fun canGetFullDate() {
		// Arrange
		val expectedResult = "November 6, 2019 13:30"
		
		// Act
		val actualResult = DateHelper.formatFullDate(date)
		
		// Assert
		Assert.assertNotEquals("", actualResult)
		Assert.assertEquals(expectedResult, actualResult)
	}
	
	@Test
	fun canGetTimePasted() {
		// Arrange
		val aDay = DateHelper.DAY
		val millisecond = Date().time - DateHelper.DAY // get today - a day
		
		// Act
		val actualResult = Date(millisecond).millisecondsSince()
		
		// Assert
		Assert.assertTrue(actualResult >= aDay) // not less than a day
	}
	
	@Test
	fun canParseLegacyDates() {
		// Arrange
		val expected1 = Date(2019, 11, 6, 6, 30, 5)
		val dateFormat1 = "2019-11-06T13:30:05.000+0700" // expected
		val expected2 = Date(2019, 11, 6, 13, 30, 5)
		val dateFormat2 = "2019-11-06 13:30:05" // expected
		val dateFormat3 = "November 6, 2019 13:30" // unexpected
		
		// Act
		val actual1 = legacyDateParser(dateFormat1)
		val actual2 = legacyDateParser(dateFormat2)
		val actual3 = legacyDateParser(dateFormat3)
		
		// Assert
		Assert.assertEquals(expected1, actual1)
		Assert.assertEquals(expected2, actual2)
		Assert.assertNull(actual3)
	}
}