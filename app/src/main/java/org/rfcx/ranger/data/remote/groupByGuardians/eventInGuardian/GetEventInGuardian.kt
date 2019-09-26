package org.rfcx.ranger.data.remote.groupByGuardians.eventInGuardian

import io.reactivex.Single
import org.rfcx.ranger.data.remote.data.alert.EventRepository
import org.rfcx.ranger.data.remote.domain.SingleUseCase
import org.rfcx.ranger.data.remote.domain.executor.PostExecutionThread
import org.rfcx.ranger.data.remote.domain.executor.ThreadExecutor
import org.rfcx.ranger.entity.event.EventResponse
import org.rfcx.ranger.entity.event.EventsGuardianRequestFactory

class GetEventInGuardian(private val eventRepository: EventRepository,
                         threadExecutor: ThreadExecutor, postExecutionThread: PostExecutionThread
) : SingleUseCase<EventsGuardianRequestFactory, EventResponse>(threadExecutor, postExecutionThread) {
	override fun buildUseCaseObservable(params: EventsGuardianRequestFactory): Single<EventResponse> {
		return eventRepository.getEventsGuardian(params)
	}
}