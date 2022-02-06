package org.rfcx.incidents.data.remote

import io.reactivex.Single
import org.rfcx.incidents.data.interfaces.EventsRepository
import org.rfcx.incidents.data.local.EventDb
import org.rfcx.incidents.entity.event.Event

class EventsRepositoryImpl(
    private val eventDb: EventDb
): EventsRepository {
    override fun get(streamId: String): Single<List<Event>> {
        return Single.just(eventDb.getEvents(streamId))
    }
}
