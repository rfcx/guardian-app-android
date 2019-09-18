package org.rfcx.ranger.data.remote.groupByGuardians

import io.reactivex.Single
import org.rfcx.ranger.data.remote.domain.SingleUseCase
import org.rfcx.ranger.data.remote.domain.executor.PostExecutionThread
import org.rfcx.ranger.data.remote.domain.executor.ThreadExecutor
import org.rfcx.ranger.entity.guardian.GroupByGuardiansResponse

class GroupByGuardiansUseCase(private val groupByGuardiansRepository: GroupByGuardiansRepository,
                              threadExecutor: ThreadExecutor, postExecutionThread: PostExecutionThread
) : SingleUseCase<String, GroupByGuardiansResponse>(threadExecutor, postExecutionThread) {
	override fun buildUseCaseObservable(params: String): Single<GroupByGuardiansResponse> {
		return groupByGuardiansRepository.sendShortName(params)
	}
}