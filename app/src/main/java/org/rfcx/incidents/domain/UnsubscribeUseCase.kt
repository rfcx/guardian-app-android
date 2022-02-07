package org.rfcx.incidents.domain

import io.reactivex.Single
import org.rfcx.incidents.data.interfaces.SubscribeRepository
import org.rfcx.incidents.domain.base.SingleUseCase
import org.rfcx.incidents.domain.executor.PostExecutionThread
import org.rfcx.incidents.domain.executor.ThreadExecutor
import org.rfcx.incidents.entity.user.SubscribeRequest
import org.rfcx.incidents.entity.user.SubscribeResponse

class UnsubscribeUseCase(
    private val subscribeRepository: SubscribeRepository,
    threadExecutor: ThreadExecutor,
    postExecutionThread: PostExecutionThread
) : SingleUseCase<SubscribeRequest, SubscribeResponse>(threadExecutor, postExecutionThread) {
    override fun buildUseCaseObservable(params: SubscribeRequest): Single<SubscribeResponse> {
        return subscribeRepository.unsubscribe(params)
    }
}
