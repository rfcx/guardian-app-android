package org.rfcx.incidents.data.remote.events

import io.reactivex.Single

class EventsRepositoryImp(private val endpoint: EventsEndpoint) : EventsRepository {
    override fun getEvents(id: String): Single<List<ResponseEvent>> {
        return endpoint.getProjects(id)
    }
}
