package org.rfcx.ranger.data.remote.guardianGroup

import io.reactivex.Single
import org.rfcx.ranger.data.remote.guardianGroup.GuardianGroupRepository
import org.rfcx.ranger.data.remote.domain.SingleUseCase
import org.rfcx.ranger.data.remote.domain.executor.PostExecutionThread
import org.rfcx.ranger.data.remote.domain.executor.ThreadExecutor
import org.rfcx.ranger.entity.event.GuardianGroupFactory
import org.rfcx.ranger.entity.guardian.GuardianGroup

class GetGuardianGroups(private val eventRepository: GuardianGroupRepository, threadExecutor: ThreadExecutor, postExecutionThread: PostExecutionThread) :
		SingleUseCase<GuardianGroupFactory, List<GuardianGroup>>(threadExecutor, postExecutionThread) {
	override fun buildUseCaseObservable(params: GuardianGroupFactory): Single<List<GuardianGroup>> {
		return eventRepository.getGuardianGroups()
	}
}