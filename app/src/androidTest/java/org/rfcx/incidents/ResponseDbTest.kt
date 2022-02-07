package org.rfcx.incidents

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import io.realm.Realm
import io.realm.RealmConfiguration
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.rfcx.incidents.data.local.ResponseDb
import org.rfcx.incidents.entity.response.Response
import org.rfcx.incidents.entity.response.SyncState

class ResponseDbTest {
    private lateinit var responseDb: ResponseDb

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @Before
    fun setup() {
        val realm = realm()
        responseDb = ResponseDb(realm)
    }

    @Test
    fun canSaveReport() {
        // Arrange
        val expectedId = 1
        val expectedValue = "xyz"

        // Act
        responseDb.save(response(1, "random-string", expectedValue, SyncState.UNSENT))

        // Assert
        val actual = responseDb.getResponseById(expectedId)
        Assert.assertEquals(expectedValue, actual?.streamId)
    }

    @Test
    fun canCountUnsent() {
        // Arrange
        val expectedUnsent = 2L
        responseDb.save(response(1, "random-string-1", "xxx", SyncState.UNSENT))
        responseDb.save(response(2, "random-string-2", "xxx", SyncState.SENT))
        responseDb.save(response(3, "random-string-3", "yyy", SyncState.UNSENT))

        // Act
        val actualUnsent = responseDb.unsentCount()

        // Assert
        Assert.assertEquals(expectedUnsent, actualUnsent)
    }

    @Test
    fun canMarkSentReport() {
        // Arrange
        val response = response(4, "random-string-4", "yyy", SyncState.UNSENT)
        responseDb.save(response)

        // Act
        responseDb.markSent(response.id, "abbc", "123")

        // Assert
        val actual = responseDb.getResponseById(response.id)
        Assert.assertEquals(SyncState.SENT.value, actual?.syncState)
    }

    private fun realm(): Realm {
        val config = RealmConfiguration.Builder()
            .name("myrealm.realm")
            .inMemory()
            .build()
        return Realm.getInstance(config)
    }

    private fun response(id: Int, guid: String, streamId: String, syncState: SyncState): Response {
        return Response(
            id = id,
            guid = guid,
            streamId = streamId,
            syncState = syncState.value
        )
    }
}
