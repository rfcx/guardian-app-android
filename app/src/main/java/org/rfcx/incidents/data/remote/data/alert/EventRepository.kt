package org.rfcx.incidents.data.remote.data.alert

import io.reactivex.Single
import org.rfcx.incidents.entity.event.*

interface EventRepository {
	
	fun getRemoteEventList(requestFactory: EventsRequestFactory): Single<EventsResponse>
	
	fun reviewEvent(requestFactory: ReviewEventFactory): Single<Unit>
	
	fun getEventsGuardian(requestFactory: EventsGuardianRequestFactory): Single<List<Event>>
	
	fun getEvent(eventGuID:String) : Single<Event>
}
