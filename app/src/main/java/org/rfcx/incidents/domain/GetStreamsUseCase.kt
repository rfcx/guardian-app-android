package org.rfcx.incidents.domain

import io.reactivex.Single
import org.rfcx.incidents.data.interfaces.StreamsRepository
import org.rfcx.incidents.domain.base.SingleUseCase
import org.rfcx.incidents.domain.executor.PostExecutionThread
import org.rfcx.incidents.domain.executor.ThreadExecutor
import org.rfcx.incidents.entity.Stream

class GetStreamsUseCase(
    private val repository: StreamsRepository,
    threadExecutor: ThreadExecutor,
    postExecutionThread: PostExecutionThread
) : SingleUseCase<GetStreamsParams, List<Stream>>(threadExecutor, postExecutionThread) {

    override fun buildUseCaseObservable(params: GetStreamsParams): Single<List<Stream>> {
        return repository.get(params.projectId, params.forceRefresh)
    }
}

data class GetStreamsParams(
    val projectId: String,
    val forceRefresh: Boolean = false,
    val more: Boolean = false
)
