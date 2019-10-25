package org.rfcx.ranger.data.remote.domain.alert

import io.reactivex.Single
import org.rfcx.ranger.data.local.EventDb
import org.rfcx.ranger.data.local.WeeklySummaryData
import org.rfcx.ranger.data.remote.data.alert.EventRepository
import org.rfcx.ranger.data.remote.service.rest.EventService
import org.rfcx.ranger.entity.event.*
import org.rfcx.ranger.util.toIsoString

class EventRepositoryImp(private val eventService: EventService, private val eventDb: EventDb,
                         private val weeklySummaryData: WeeklySummaryData) : EventRepository {
	
	override fun getEventsGuardian(requestFactory: EventsGuardianRequestFactory): Single<EventResponse> {
		return eventService.getEventsGuardian(requestFactory.guardian, requestFactory.value, requestFactory.time.toIsoString(), requestFactory.orderBy,
				requestFactory.dir, requestFactory.limit, requestFactory.offset).map {
			it
		}
	}
	
	override fun getRemoteEventList(requestFactory: EventsRequestFactory): Single<EventResponse> {
		return eventService.getEvents(requestFactory.limit, requestFactory.offset, requestFactory.order,
				requestFactory.dir, requestFactory.guardianInGroup ).map { it ->
			
			if (requestFactory.offset == 0) {
				it.events?.let {
					eventDb.saveEvents(it)
				}
			}
			it
		}
	}
	
	override fun reviewEvent(requestFactory: ReviewEventFactory): Single<Unit> {
		val reviewedEventState = eventDb.getEventState(requestFactory.eventGuID)
		if (reviewedEventState == null)
			weeklySummaryData.adJustReviewCount()
		
		eventDb.save(EventReview(requestFactory.eventGuID, requestFactory.reviewConfirm,
				EventReview.UNSENT))
		return Single.just(Unit)
	}
}