package org.rfcx.incidents.data.remote.subscribe

import io.reactivex.Single
import org.rfcx.incidents.data.remote.domain.SingleUseCase
import org.rfcx.incidents.data.remote.domain.executor.PostExecutionThread
import org.rfcx.incidents.data.remote.domain.executor.ThreadExecutor
import org.rfcx.incidents.entity.SubscribeRequest
import org.rfcx.incidents.entity.SubscribeResponse

class SubscribeUseCase(
    private val subscribeRepository: SubscribeRepository,
    threadExecutor: ThreadExecutor,
    postExecutionThread: PostExecutionThread
) : SingleUseCase<SubscribeRequest, SubscribeResponse>(threadExecutor, postExecutionThread) {
    override fun buildUseCaseObservable(params: SubscribeRequest): Single<SubscribeResponse> {
        return subscribeRepository.sendBody(params)
    }
}
