package org.rfcx.ranger.data.remote.domain.alert

import io.reactivex.Single
import org.rfcx.ranger.data.remote.data.alert.EventRepository
import org.rfcx.ranger.data.remote.domain.SingleUseCase
import org.rfcx.ranger.data.remote.domain.executor.PostExecutionThread
import org.rfcx.ranger.data.remote.domain.executor.ThreadExecutor
import org.rfcx.ranger.entity.event.Event
import org.rfcx.ranger.entity.event.EventResponse
import org.rfcx.ranger.entity.event.EventsResponse
import org.rfcx.ranger.entity.event.EventsRequestFactory

class GetEventUseCase(private val eventRepository: EventRepository, threadExecutor: ThreadExecutor, postExecutionThread: PostExecutionThread) :
		SingleUseCase<String, Event>(threadExecutor, postExecutionThread) {
	override fun buildUseCaseObservable(params: String): Single<Event> {
		return eventRepository.getEvent(params)
	}
}