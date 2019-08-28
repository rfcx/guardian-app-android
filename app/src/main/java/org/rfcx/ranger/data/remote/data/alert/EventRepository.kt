package org.rfcx.ranger.data.remote.data.alert

import io.reactivex.Single
import org.rfcx.ranger.entity.event.*

interface EventRepository {
	
	fun getEventList(requestFactory: EventsRequestFactory): Single<EventResponse>
	
	fun reviewEvent(requestFactory: ReviewEventFactory): Single<ReviewEventResponse>
	
}