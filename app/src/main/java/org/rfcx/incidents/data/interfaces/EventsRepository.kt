package org.rfcx.incidents.data.interfaces

import io.reactivex.Single
import org.rfcx.incidents.entity.event.Event

interface EventsRepository {
    fun get(streamId: String): Single<List<Event>>
}
