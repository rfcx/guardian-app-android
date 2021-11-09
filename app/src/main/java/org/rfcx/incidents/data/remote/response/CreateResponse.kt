package org.rfcx.incidents.data.remote.response

import io.reactivex.Single
import org.rfcx.incidents.data.remote.domain.SingleUseCase
import org.rfcx.incidents.data.remote.domain.executor.PostExecutionThread
import org.rfcx.incidents.data.remote.domain.executor.ThreadExecutor

class CreateResponse(private val repository: CreateResponseRepository,
                     threadExecutor: ThreadExecutor, postExecutionThread: PostExecutionThread
) : SingleUseCase<CreateResponseRequest, CreateResponseRes>(threadExecutor, postExecutionThread) {
	override fun buildUseCaseObservable(params: CreateResponseRequest): Single<CreateResponseRes> {
		return repository.createResponseRequest(params)
	}
}
