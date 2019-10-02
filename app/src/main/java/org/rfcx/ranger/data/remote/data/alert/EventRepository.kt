package org.rfcx.ranger.data.remote.data.alert

import io.reactivex.Single
import org.rfcx.ranger.entity.event.*

interface EventRepository {
	
	fun getRemoteEventList(requestFactory: EventsRequestFactory): Single<EventResponse>
	
	fun reviewEvent(requestFactory: ReviewEventFactory): Single<ReviewEventResponse>
	
	fun getEventsGuardian(requestFactory: EventsGuardianRequestFactory): Single<EventResponse>
	
	fun getLocalEvents(): Single<List<Event>>
}