package org.rfcx.ranger

import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.rfcx.ranger.util.DateHelper
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
	fun canGetIsoDateString() { //TODO: Improve @Aa
		// Arrange
		val expectedResult = "2019-11-06T13:30:05.000+0700"
		
		// Act
		val actualResult = DateHelper.getIsoTime(date)
		
		// Assert
		Assert.assertNotEquals("", actualResult)
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
	fun canGetFormatDateTimeSecond() {
		// Arrange
		val expectedResult = "2019-11-06 13:30:05"
		
		// Act
		val actualResult = DateHelper.formatDateTimeSecond(date)
		
		// Assert
		Assert.assertNotEquals("", actualResult)
		Assert.assertEquals(expectedResult, actualResult)
	}
	
	@Test
	fun canGetFormatTime() {
		// Arrange
		val expectedResult = "13:30"
		
		// Act
		val actualResult = DateHelper.formatTime(date)
		
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
		val actualResult = DateHelper.getTimePasted(Date(millisecond))
		
		// Assert
		Assert.assertTrue(actualResult >= aDay) // not less than a day
	}
	
	@Test
	fun canParseToDate() {
		// Arrange
		val dateFormat1 = "2019-11-06T13:30:05.000+0700" // expected
		val dateFormat2 = "2019-11-06 13:30:05" // expected
		val dateFormat3 = "November 6, 2019 13:30" // unexpected
		
		// Act
		val actualResultFormat1 = DateHelper.legacyParseToDate(dateFormat1)
		val actualResultFormat2 = DateHelper.legacyParseToDate(dateFormat2)
		val actualResultFormat3 = DateHelper.legacyParseToDate(dateFormat3)
		
		// Assert
		Assert.assertNotNull(actualResultFormat1)
		Assert.assertNotNull(actualResultFormat2)
		Assert.assertNull(actualResultFormat3)
	}
}