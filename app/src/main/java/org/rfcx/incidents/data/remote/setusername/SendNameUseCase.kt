package org.rfcx.incidents.data.remote.setusername

import io.reactivex.Single
import org.rfcx.incidents.data.remote.domain.SingleUseCase
import org.rfcx.incidents.data.remote.domain.executor.PostExecutionThread
import org.rfcx.incidents.data.remote.domain.executor.ThreadExecutor
import org.rfcx.incidents.entity.user.SetNameRequest
import org.rfcx.incidents.entity.user.SetNameResponse

class SendNameUseCase(
    private val setNameRepository: SetNameRepository,
    threadExecutor: ThreadExecutor,
    postExecutionThread: PostExecutionThread
) : SingleUseCase<SetNameRequest, SetNameResponse>(threadExecutor, postExecutionThread) {
    override fun buildUseCaseObservable(params: SetNameRequest): Single<SetNameResponse> {
        return setNameRepository.sendName(params)
    }
}
