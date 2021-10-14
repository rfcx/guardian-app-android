package org.rfcx.ranger.data.api.events

import io.reactivex.Single
import org.rfcx.ranger.data.remote.domain.SingleUseCase
import org.rfcx.ranger.data.remote.domain.executor.PostExecutionThread
import org.rfcx.ranger.data.remote.domain.executor.ThreadExecutor

class GetEvents(private val repository: EventsRepository,
                threadExecutor: ThreadExecutor, postExecutionThread: PostExecutionThread) : SingleUseCase<String, List<ResponseEvent>>(threadExecutor, postExecutionThread) {
	override fun buildUseCaseObservable(params: String): Single<List<ResponseEvent>> {
		return repository.getEvents(params)
	}
}
