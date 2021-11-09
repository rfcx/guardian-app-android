package org.rfcx.incidents.data.remote.domain.alert

import io.reactivex.Single
import org.rfcx.incidents.data.remote.data.alert.EventRepository
import org.rfcx.incidents.data.remote.domain.SingleUseCase
import org.rfcx.incidents.data.remote.domain.executor.PostExecutionThread
import org.rfcx.incidents.data.remote.domain.executor.ThreadExecutor
import org.rfcx.incidents.entity.event.Event

class GetEventUseCase(private val eventRepository: EventRepository, threadExecutor: ThreadExecutor, postExecutionThread: PostExecutionThread) :
		SingleUseCase<String, Event>(threadExecutor, postExecutionThread) {
	override fun buildUseCaseObservable(params: String): Single<Event> {
		return eventRepository.getEvent(params)
	}
}
