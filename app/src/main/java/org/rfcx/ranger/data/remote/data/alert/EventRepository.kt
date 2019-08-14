package org.rfcx.ranger.data.remote.data.alert

import io.reactivex.Single
import org.rfcx.ranger.entity.event.Event
import org.rfcx.ranger.entity.event.EventsRequestFactory
import org.rfcx.ranger.entity.event.ReviewEventFactory
import org.rfcx.ranger.entity.event.ReviewEventResponse

interface EventRepository {
	
	fun getEventList(requestFactory: EventsRequestFactory): Single<List<Event>>
	
	fun reviewEvent(requestFactory: ReviewEventFactory): Single<ReviewEventResponse>
	
}