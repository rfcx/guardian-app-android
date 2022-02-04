package org.rfcx.incidents.domain

import io.reactivex.Single
import org.rfcx.incidents.data.interfaces.EventsRepository
import org.rfcx.incidents.data.remote.events.ResponseEvent
import org.rfcx.incidents.domain.base.SingleUseCase
import org.rfcx.incidents.domain.executor.PostExecutionThread
import org.rfcx.incidents.domain.executor.ThreadExecutor

class GetEventsUseCase(
    private val repository: EventsRepository,
    threadExecutor: ThreadExecutor,
    postExecutionThread: PostExecutionThread
) : SingleUseCase<String, List<ResponseEvent>>(threadExecutor, postExecutionThread) {
    override fun buildUseCaseObservable(params: String): Single<List<ResponseEvent>> {
        return repository.getEvents(params)
    }
}
