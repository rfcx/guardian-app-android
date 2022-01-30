package org.rfcx.incidents.data.api.streams

import io.reactivex.Single
import org.rfcx.incidents.data.remote.domain.SingleUseCase
import org.rfcx.incidents.data.remote.domain.executor.PostExecutionThread
import org.rfcx.incidents.data.remote.domain.executor.ThreadExecutor

class GetStreamsUseCase(
    private val repository: GetStreamsRepository,
    threadExecutor: ThreadExecutor,
    postExecutionThread: PostExecutionThread
) : SingleUseCase<StreamsRequestFactory, List<StreamResponse>>(threadExecutor, postExecutionThread) {
    override fun buildUseCaseObservable(params: StreamsRequestFactory): Single<List<StreamResponse>> {
        return repository.getStreams(params)
    }
}
