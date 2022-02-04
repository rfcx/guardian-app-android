package org.rfcx.incidents.data.interfaces

import io.reactivex.Single
import org.rfcx.incidents.data.remote.events.ResponseEvent

interface EventsRepository {
    fun getEvents(id: String): Single<List<ResponseEvent>>
}
