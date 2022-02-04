package org.rfcx.incidents.domain

import io.reactivex.Single
import org.rfcx.incidents.data.interfaces.GetStreamsRepository
import org.rfcx.incidents.data.remote.streams.StreamResponse
import org.rfcx.incidents.data.remote.streams.StreamsRequestFactory
import org.rfcx.incidents.domain.base.SingleUseCase
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
