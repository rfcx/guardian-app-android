package org.rfcx.incidents.domain

import io.reactivex.Single
import org.rfcx.incidents.domain.base.SingleUseCase
import org.rfcx.incidents.domain.executor.PostExecutionThread
import org.rfcx.incidents.domain.executor.ThreadExecutor
import org.rfcx.incidents.entity.event.Event

class GetEventsUseCase(
    threadExecutor: ThreadExecutor,
    postExecutionThread: PostExecutionThread
) : SingleUseCase<String, List<Event>>(threadExecutor, postExecutionThread) {
    override fun buildUseCaseObservable(params: String): Single<List<Event>> {
        return Single.just(listOf())
    }
}
