package org.rfcx.ranger.data.remote.data.alert

import io.reactivex.Single
import org.rfcx.ranger.entity.event.*

interface EventRepository {
	
	fun getRemoteEventList(requestFactory: EventsRequestFactory): Single<EventsResponse>
	
	fun reviewEvent(requestFactory: ReviewEventFactory): Single<Unit>
	
	fun getEventsGuardian(requestFactory: EventsGuardianRequestFactory): Single<EventsResponse>
}