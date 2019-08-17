package org.rfcx.ranger.data.remote.domain.alert

import io.reactivex.Single
import org.rfcx.ranger.data.local.EventDb
import org.rfcx.ranger.data.remote.data.alert.EventRepository
import org.rfcx.ranger.data.remote.service.rest.EventService
import org.rfcx.ranger.entity.event.*

class EventRepositoryImp(private val eventService: EventService, private val eventDb: EventDb) : EventRepository {
	override fun getEventList(requestFactory: EventsRequestFactory): Single<EventResponse> {
		return eventService.getEvents(requestFactory.guardianGroup, requestFactory.orderBy,
				requestFactory.dir, requestFactory.limit, requestFactory.offset).map {
			it
		}
	}
	
	override fun reviewEvent(requestFactory: ReviewEventFactory): Single<ReviewEventResponse> {
		return eventService.reviewEvent(requestFactory.eventGuID, requestFactory.reviewConfirm).flatMap {
			eventDb.save(EventRealmObject(requestFactory.eventGuID, requestFactory.reviewConfirm))
			Single.just(it)
		}
	}
}