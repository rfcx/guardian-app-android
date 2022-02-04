package org.rfcx.incidents.domain

import io.reactivex.Single
import org.rfcx.incidents.data.SetNameRepository
import org.rfcx.incidents.domain.executor.PostExecutionThread
import org.rfcx.incidents.domain.executor.ThreadExecutor
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
