package org.rfcx.incidents

import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.rfcx.incidents.util.legacyDateParser
import org.rfcx.incidents.util.millisecondsSince
import org.rfcx.incidents.util.setShortTimeZone
import org.rfcx.incidents.util.toDateWithTimeZone
import org.rfcx.incidents.util.toFullDateTimeString
import org.rfcx.incidents.util.toIsoString
import org.rfcx.incidents.util.toShortDateString
import org.rfcx.incidents.util.toTimeString
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
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
    fun canParseLegacyDateFormatFromRealm() {
        // Arrange
        val calendar = Calendar.getInstance()
        calendar.set(2019, 10, 6, 13, 30, 0)
        calendar.timeZone = TimeZone.getTimeZone("UTC")
        val expected = calendar.time
        val dateFormatter = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        val dateAsString = dateFormatter.format(expected)

        // Act
        val actual = legacyDateParser(dateAsString)

        // Assert (number of seconds)
        Assert.assertEquals(expected.time / 1000, actual!!.time / 1000)
    }

    @Test
    fun canParseLegacyDateFormatWithTimezone() {
        // Arrange
        val calendar = Calendar.getInstance()
        calendar.set(2019, 10, 6, 13, 30, 5)
        calendar.timeZone = TimeZone.getTimeZone("UTC")
        val expected = calendar.time
        val dateFormat1 = "2019-11-06T13:30:05.000+0000"
        val dateFormat2 = "2019-11-06T20:30:05.000+0700"
        val dateFormat3 = "2019-11-06T10:30:05.000-0300"

        // Act
        val actual1 = legacyDateParser(dateFormat1)
        val actual2 = legacyDateParser(dateFormat2)
        val actual3 = legacyDateParser(dateFormat3)

        // Assert (number of seconds)
        Assert.assertEquals(expected.time / 1000, actual1!!.time / 1000)
        Assert.assertEquals(expected.time / 1000, actual2!!.time / 1000)
        Assert.assertEquals(expected.time / 1000, actual3!!.time / 1000)
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

    @Test
    fun canGetShortDateString() {

        // Arrange
        val expected1 = "22 Feb - 24 Feb (MST)"
        val expected2 = "22 Feb, 23:49 (MST)"
        val expected3 = "8 Mar, 14:09 (GMT-1)"
        val expected4 = "1 Aug 2021, 16:30 (EST)"
        val expected5 = "23 Feb, 01:35 (EST)"
        val expected6 = "23 Feb, 18:00 (GMT+2)"
        val expected7 = "23 Feb, 11:42 (GMT+10:30)"

        // Act
        val actual1 = setShortTimeZone("22 Feb - 24 Feb (MST)")
        val actual2 = setShortTimeZone("22 Feb, 23:49 (MST)")
        val actual3 = setShortTimeZone("8 Mar, 14:09 (GMT-01:00)")
        val actual4 = setShortTimeZone("1 Aug 2021, 16:30 (EST)")
        val actual5 = setShortTimeZone("23 Feb, 01:35 (EST)")
        val actual6 = setShortTimeZone("23 Feb, 18:00 (GMT+02:00)")
        val actual7 = setShortTimeZone("23 Feb, 11:42 (GMT+10:30)")

        // Assert
        Assert.assertEquals(expected1, actual1) // 22 Feb - 24 Feb (MST)
        Assert.assertEquals(expected2, actual2) // 22 Feb, 23:49 (MST)
        Assert.assertEquals(expected3, actual3) // 8 Mar, 14:09 (GMT-01:00)
        Assert.assertEquals(expected4, actual4) // 1 Aug 2021, 16:30 (EST)
        Assert.assertEquals(expected5, actual5) // 23 Feb, 01:35 (EST)
        Assert.assertEquals(expected6, actual6) // 23 Feb, 18:00 (GMT+02:00)
        Assert.assertEquals(expected7, actual7) // 23 Feb, 11:42 (GMT+10:30)

    }
}
