package org.rfcx.incidents.data.remote.domain.alert

import io.reactivex.Single
import org.rfcx.incidents.data.remote.data.alert.EventRepository
import org.rfcx.incidents.data.remote.domain.SingleUseCase
import org.rfcx.incidents.data.remote.domain.executor.PostExecutionThread
import org.rfcx.incidents.data.remote.domain.executor.ThreadExecutor
import org.rfcx.incidents.entity.event.ReviewEventFactory

class ReviewEventUseCase(private val eventRepository: EventRepository, threadExecutor: ThreadExecutor,
                         postExecutionThread: PostExecutionThread) : SingleUseCase<ReviewEventFactory, Unit>(
		threadExecutor, postExecutionThread
) {
	override fun buildUseCaseObservable(params: ReviewEventFactory): Single<Unit> {
		return eventRepository.reviewEvent(params)
	}
}
