package org.rfcx.incidents.data.remote.streams

import io.reactivex.Single
import org.rfcx.incidents.domain.SingleUseCase
import org.rfcx.incidents.domain.executor.PostExecutionThread
import org.rfcx.incidents.domain.executor.ThreadExecutor

class GetStreamsUseCase(
    private val repository: GetStreamsRepository,
    threadExecutor: ThreadExecutor,
    postExecutionThread: PostExecutionThread
) : SingleUseCase<StreamsRequestFactory, List<StreamResponse>>(threadExecutor, postExecutionThread) {
    override fun buildUseCaseObservable(params: StreamsRequestFactory): Single<List<StreamResponse>> {
        return repository.getStreams(params)
    }
}
