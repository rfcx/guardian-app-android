package org.rfcx.incidents.data.remote.subscribe.unsubscribe

import io.reactivex.Single
import org.rfcx.incidents.data.remote.domain.SingleUseCase
import org.rfcx.incidents.data.remote.domain.executor.PostExecutionThread
import org.rfcx.incidents.data.remote.domain.executor.ThreadExecutor
import org.rfcx.incidents.entity.SubscribeRequest
import org.rfcx.incidents.entity.SubscribeResponse

class UnsubscribeUseCase(
    private val unsubscribeRepository: UnsubscribeRepository,
    threadExecutor: ThreadExecutor, postExecutionThread: PostExecutionThread
) : SingleUseCase<SubscribeRequest, SubscribeResponse>(threadExecutor, postExecutionThread) {
    override fun buildUseCaseObservable(params: SubscribeRequest): Single<SubscribeResponse> {
        return unsubscribeRepository.sendUnsubscribeBody(params)
    }
}
