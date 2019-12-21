package org.rfcx.ranger.data.remote.groupByGuardians.eventInGuardian

import io.reactivex.Single
import org.rfcx.ranger.data.remote.data.alert.EventRepository
import org.rfcx.ranger.data.remote.domain.SingleUseCase
import org.rfcx.ranger.data.remote.domain.executor.PostExecutionThread
import org.rfcx.ranger.data.remote.domain.executor.ThreadExecutor
import org.rfcx.ranger.entity.event.Event
import org.rfcx.ranger.entity.event.EventsGuardianRequestFactory

class GetMoreEventInGuardian(private val eventRepository: EventRepository,
                             threadExecutor: ThreadExecutor, postExecutionThread: PostExecutionThread
) : SingleUseCase<EventsGuardianRequestFactory, List<Event>>(threadExecutor, postExecutionThread) {
	override fun buildUseCaseObservable(params: EventsGuardianRequestFactory): Single<List<Event>> {
		return eventRepository.getEventsGuardian(params)
	}
}