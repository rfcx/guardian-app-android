package org.rfcx.ranger.data.remote.groupByGuardians.eventInGuardian

import io.reactivex.Single
import org.rfcx.ranger.data.remote.domain.SingleUseCase
import org.rfcx.ranger.data.remote.domain.executor.PostExecutionThread
import org.rfcx.ranger.data.remote.domain.executor.ThreadExecutor
import org.rfcx.ranger.entity.event.EventInGuardianResponse

class GetEventInGuardian(private val eventInGuardianRepository: EventInGuardianRepository,
                         threadExecutor: ThreadExecutor, postExecutionThread: PostExecutionThread
) : SingleUseCase<String, EventInGuardianResponse>(threadExecutor, postExecutionThread) {
	override fun buildUseCaseObservable(params: String): Single<EventInGuardianResponse> {
		return eventInGuardianRepository.getEventInGuardian(params)
	}
}