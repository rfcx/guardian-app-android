package org.rfcx.ranger.data.remote.domain.alert

import io.reactivex.Single
import org.rfcx.ranger.data.remote.data.alert.EventRepository
import org.rfcx.ranger.data.remote.service.rest.EventService
import org.rfcx.ranger.entity.event.Event
import org.rfcx.ranger.entity.event.EventsRequestFactory
import org.rfcx.ranger.entity.event.ReviewEventFactory
import org.rfcx.ranger.entity.event.ReviewEventResponse

class EventRepositoryImp(private val eventService: EventService) : EventRepository {
	override fun getEventList(requestFactory: EventsRequestFactory): Single<List<Event>> {
		return eventService.getEvents(requestFactory.guardianGroup, requestFactory.orderBy,
				requestFactory.dir, requestFactory.limit, requestFactory.offset).map {
			it.events
		}
	}
	
	override fun reviewEvent(requestFactory: ReviewEventFactory): Single<ReviewEventResponse> {
		return eventService.reviewEvent(requestFactory.eventGuID, requestFactory.reviewConfirm)
	}
}