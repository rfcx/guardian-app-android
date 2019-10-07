package org.rfcx.ranger.data.remote.data.alert

import io.reactivex.Single
import org.rfcx.ranger.entity.event.Event
import org.rfcx.ranger.entity.event.EventResponse
import org.rfcx.ranger.entity.event.EventsRequestFactory
import org.rfcx.ranger.entity.event.ReviewEventFactory

interface EventRepository {
	
	fun getRemoteEventList(requestFactory: EventsRequestFactory): Single<EventResponse>
	
	fun reviewEvent(requestFactory: ReviewEventFactory): Single<Unit>
	
	fun getEventsGuardian(requestFactory: EventsGuardianRequestFactory): Single<EventResponse>
	
	fun getLocalEvents(): Single<List<Event>>
}