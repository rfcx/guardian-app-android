package org.rfcx.incidents.data.remote.events

import io.reactivex.Single
import org.rfcx.incidents.domain.SingleUseCase
import org.rfcx.incidents.domain.executor.PostExecutionThread
import org.rfcx.incidents.domain.executor.ThreadExecutor

class GetEvents(
    private val repository: EventsRepository,
    threadExecutor: ThreadExecutor,
    postExecutionThread: PostExecutionThread
) : SingleUseCase<String, List<ResponseEvent>>(threadExecutor, postExecutionThread) {
    override fun buildUseCaseObservable(params: String): Single<List<ResponseEvent>> {
        return repository.getEvents(params)
    }
}
