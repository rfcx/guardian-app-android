package org.rfcx.incidents.data

import io.reactivex.Single
import org.rfcx.incidents.data.interfaces.EventsRepository
import org.rfcx.incidents.data.remote.events.EventsEndpoint
import org.rfcx.incidents.data.remote.events.ResponseEvent

class EventsRepositoryImp(private val endpoint: EventsEndpoint) : EventsRepository {
    override fun getEvents(id: String): Single<List<ResponseEvent>> {
        return endpoint.getProjects(id)
    }
}
