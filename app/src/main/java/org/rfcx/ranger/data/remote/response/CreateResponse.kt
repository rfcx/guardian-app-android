package org.rfcx.ranger.data.remote.response

import io.reactivex.Single
import okhttp3.ResponseBody
import org.rfcx.ranger.data.remote.domain.SingleUseCase
import org.rfcx.ranger.data.remote.domain.executor.PostExecutionThread
import org.rfcx.ranger.data.remote.domain.executor.ThreadExecutor

class CreateResponse(private val repository: CreateResponseRepository,
                     threadExecutor: ThreadExecutor, postExecutionThread: PostExecutionThread
) : SingleUseCase<CreateResponseRequest, ResponseBody>(threadExecutor, postExecutionThread) {
	override fun buildUseCaseObservable(params: CreateResponseRequest): Single<ResponseBody> {
		return repository.createResponseRequest(params)
	}
}
