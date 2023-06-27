package org.rfcx.incidents.data

import android.util.Log
import io.reactivex.Single
import org.rfcx.incidents.data.interfaces.EventsRepository
import org.rfcx.incidents.data.local.StreamDb
import org.rfcx.incidents.entity.event.Event

class EventsRepositoryImpl(
    private val streamDb: StreamDb
) : EventsRepository {
    override fun get(streamId: String): Single<List<Event>> {
        val stream = streamDb.get(streamId)
        Log.d("GuardianAPp", "${stream?.lastIncident?.events?.toString()}")
        return Single.just(stream?.lastIncident?.events)
    }
}
