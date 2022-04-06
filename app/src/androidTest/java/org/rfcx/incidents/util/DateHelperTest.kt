package org.rfcx.incidents.util

import android.content.Context
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import java.util.Calendar
import java.util.TimeZone

class DateHelperTest {
    private lateinit var context: Context

    @Before
    fun setup() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
    }

    @Test
    fun canGetDateStringWithTimeZone() {
        // Arrange
        val expectedSameTimeZone = "6 Nov 2019, 20:30"
        val expectedNotSameTimeZone = "6 Nov 2019, 08:30 (EST)"

        // Act
        val calender = Calendar.getInstance()
        calender.set(2019, 10, 6, 13, 30, 5)
        calender.timeZone = TimeZone.getTimeZone("UTC")
        val date = calender.time

        val timezoneA = TimeZone.getTimeZone("Asia/Bangkok")
        val actualSameTimeZone = date.toStringWithTimeZone(context, timezoneA)

        val timezoneB = TimeZone.getTimeZone("America/New_York")
        val actualNotSameTimeZone = date.toStringWithTimeZone(context, timezoneB)

        // Assert
        Assert.assertEquals(expectedSameTimeZone, actualSameTimeZone)
        Assert.assertEquals(expectedNotSameTimeZone, actualNotSameTimeZone)
    }
}
