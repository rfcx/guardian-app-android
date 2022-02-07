package org.rfcx.incidents.domain

import io.reactivex.Single
import org.rfcx.incidents.data.interfaces.CreateResponseRepository
import org.rfcx.incidents.data.remote.response.CreateResponseRequest
import org.rfcx.incidents.data.remote.response.CreateResponseRes
import org.rfcx.incidents.domain.base.SingleUseCase
import org.rfcx.incidents.domain.executor.PostExecutionThread
import org.rfcx.incidents.domain.executor.ThreadExecutor

class CreateResponseUseCase(
    private val repository: CreateResponseRepository,
    threadExecutor: ThreadExecutor,
    postExecutionThread: PostExecutionThread
) : SingleUseCase<CreateResponseRequest, CreateResponseRes>(threadExecutor, postExecutionThread) {
    override fun buildUseCaseObservable(params: CreateResponseRequest): Single<CreateResponseRes> {
        return repository.createResponseRequest(params)
    }
}
