package org.rfcx.incidents

import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.rfcx.incidents.util.millisecondsSince
import org.rfcx.incidents.util.toDateWithTimeZone
import org.rfcx.incidents.util.toFullDateTimeString
import org.rfcx.incidents.util.toIsoString
import org.rfcx.incidents.util.toShortDateString
import org.rfcx.incidents.util.toTimeString
import java.util.Calendar
import java.util.Date
import java.util.TimeZone

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
    fun canGetDateWithTimeZone() {
        // Arrange
        val calendar = Calendar.getInstance()
        calendar.set(2019, 10, 6, 13, 30, 5)
        calendar.timeZone = TimeZone.getTimeZone("America/New_York")
        val expected = calendar.time

        // Act
        val calender = Calendar.getInstance()
        calender.set(2019, 10, 6, 13, 30, 5)
        calender.timeZone = TimeZone.getTimeZone("UTC")
        date = calender.time

        val timezone = TimeZone.getTimeZone("America/New_York")
        val actual = date.toDateWithTimeZone(timezone)

        // Assert
        Assert.assertEquals(expected.time / 1000, actual.time / 1000)
    }
}
