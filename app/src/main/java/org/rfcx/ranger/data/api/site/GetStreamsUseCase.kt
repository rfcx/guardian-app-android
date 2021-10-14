package org.rfcx.ranger.data.api.site

import io.reactivex.Single
import org.rfcx.ranger.data.remote.domain.SingleUseCase
import org.rfcx.ranger.data.remote.domain.executor.PostExecutionThread
import org.rfcx.ranger.data.remote.domain.executor.ThreadExecutor

class GetStreamsUseCase(private val repository: GetStreamsRepository,
                        threadExecutor: ThreadExecutor, postExecutionThread: PostExecutionThread
) : SingleUseCase<StreamsRequestFactory, List<StreamResponse>>(threadExecutor, postExecutionThread) {
	override fun buildUseCaseObservable(params: StreamsRequestFactory): Single<List<StreamResponse>> {
		return repository.getStreams(params)
	}
}
