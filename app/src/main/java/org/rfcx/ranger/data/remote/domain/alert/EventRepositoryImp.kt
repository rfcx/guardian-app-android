package org.rfcx.ranger.data.remote.domain.alert

import io.reactivex.Single
import org.rfcx.ranger.data.local.EventDb
import org.rfcx.ranger.data.local.WeeklySummaryData
import org.rfcx.ranger.data.remote.data.alert.EventRepository
import org.rfcx.ranger.data.remote.service.rest.EventService
import org.rfcx.ranger.entity.event.*
import org.rfcx.ranger.service.DownLoadEventWorker

class EventRepositoryImp(private val eventService: EventService, private val eventDb: EventDb,
                         private val weeklySummaryData: WeeklySummaryData) : EventRepository {
	
	override fun getEventsGuardian(requestFactory: EventsGuardianRequestFactory): Single<List<Event>> {
		return eventService.getEventsGuardian(requestFactory.guardian, requestFactory.value, requestFactory.orderBy,
				requestFactory.dir, requestFactory.limit, requestFactory.offset).map { r ->
			
			val events = r.events?.map { it.toEvent() } ?: listOf()
			// store events
			if (events.isNotEmpty()) {
				eventDb.saveEvents(events)
				// start worker download event for offline
				DownLoadEventWorker.enqueue()
			}
			
			events
		}
	}
	
	override fun getRemoteEventList(requestFactory: EventsRequestFactory): Single<EventsResponse> {
		return eventService.getEvents(requestFactory.limit, requestFactory.offset, requestFactory.order,
				requestFactory.dir, requestFactory.guardianInGroup, requestFactory.value)
	}
	
	override fun reviewEvent(requestFactory: ReviewEventFactory): Single<Unit> {
		val reviewedEventState = eventDb.getEventState(requestFactory.eventGuID)
		if (reviewedEventState == null)
			weeklySummaryData.adJustReviewCount()
		
		val request = ReviewEventRequest(requestFactory.reviewConfirm == "confirm", true, arrayListOf())
		return eventService.reviewEventOnline(requestFactory.eventGuID, request).map {
			eventDb.save(EventReview(requestFactory.eventGuID, requestFactory.reviewConfirm,
					if (it.success) EventReview.SENT else EventReview.UNSENT))
		}
	}
	
	override fun getEvent(eventGuID: String): Single<Event> {
		return eventService.getEvent(eventGuID).map {
			it.toEvent()
		}
	}
}