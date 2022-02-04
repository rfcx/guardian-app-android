package org.rfcx.incidents

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import io.realm.Realm
import io.realm.RealmConfiguration
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.rfcx.incidents.entity.report.Report
import org.rfcx.incidents.data.local.ReportDb
import java.util.*

class ReportDbTest {
    private lateinit var reportDb: ReportDb

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @Before
    fun setup() {
        val realm = realm()
        reportDb = ReportDb(realm)
    }

    @Test
    fun canSaveReport() {
        // Arrange
        val expectedId = 1
        val expectedValue = "vehicle"

        // Act
        reportDb.save(report(1, "random-string", "vehicle", ReportDb.UNSENT), arrayListOf())
        val actual = reportDb.getReport(expectedId)

        // Assert
        Assert.assertEquals(expectedValue, actual?.value)
    }

    @Test
    fun canCountReport() {
        // Arrange
        val expectedSent = 1L
        val expectedUnsent = 2L

        // Act
        reportDb.save(report(1, "random-string1", "vehicle", ReportDb.UNSENT), arrayListOf())
        reportDb.save(report(2, "random-string2", "gunshot", ReportDb.SENT), arrayListOf())
        reportDb.save(report(3, "random-string3", "vehicle", ReportDb.UNSENT), arrayListOf())
        val actualSent = reportDb.sentCount()
        val actualUnSent = reportDb.unsentCount()

        // Assert
        Assert.assertEquals(expectedUnsent, actualUnSent)
        Assert.assertEquals(expectedSent, actualSent)
    }

    @Test
    fun canMarkSentReport() {
        // Arrange
        val report = report(1, "random-string1", "vehicle", ReportDb.UNSENT)
        val expectedResult = ReportDb.SENT

        // Act
        reportDb.save(report, arrayListOf())
        reportDb.markSent(report.id, report.guid!!)
        val actual = reportDb.getReport(1)?.syncState

        // Assert
        Assert.assertEquals(expectedResult, actual)
    }

    @Test
    fun canMarkUnsentReport() {
        // Arrange
        val report = report(1, "random-string1", "vehicle", ReportDb.SENT)
        val expectedResult = ReportDb.UNSENT

        // Act
        reportDb.save(report, arrayListOf())
        reportDb.markUnsent(report.id)
        val actual = reportDb.getReport(1)?.syncState

        // Assert
        Assert.assertEquals(expectedResult, actual)
    }

    private fun realm(): Realm {
        val config = RealmConfiguration.Builder()
            .name("myrealm.realm")
            .inMemory()
            .build()
        return Realm.getInstance(config)
    }

    private fun report(id: Int, guid: String, value: String, syncState: Int): Report {
        return Report(
            id = id,
            guid = guid,
            value = value,
            site = "derc",
            reportedAt = Date(),
            latitude = 16.7611947,
            longitude = 100.2712304,
            syncState = syncState
        )
    }
}
