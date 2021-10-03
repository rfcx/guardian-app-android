package org.rfcx.ranger.data.api.events

import io.reactivex.Single

interface EventsRepository {
	fun getEvents(id: String): Single<List<ResponseEvent>>
}
