package org.rfcx.ranger.data.remote.subscribe.unsubscribe

import io.reactivex.Single
import org.rfcx.ranger.data.remote.domain.SingleUseCase
import org.rfcx.ranger.data.remote.domain.executor.PostExecutionThread
import org.rfcx.ranger.data.remote.domain.executor.ThreadExecutor
import org.rfcx.ranger.entity.SubscribeRequest
import org.rfcx.ranger.entity.SubscribeResponse

class UnsubscribeUseCase(private val unsubscribeRepository: UnsubscribeRepository,
                         threadExecutor: ThreadExecutor, postExecutionThread: PostExecutionThread
) : SingleUseCase<SubscribeRequest, SubscribeResponse>(threadExecutor, postExecutionThread) {
	override fun buildUseCaseObservable(params: SubscribeRequest): Single<SubscribeResponse> {
		return unsubscribeRepository.sendUnsubscribeBody(params)
	}
}