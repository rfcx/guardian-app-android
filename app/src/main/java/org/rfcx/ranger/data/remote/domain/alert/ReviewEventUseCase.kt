package org.rfcx.ranger.data.remote.domain.alert

import io.reactivex.Single
import org.rfcx.ranger.data.remote.data.alert.EventRepository
import org.rfcx.ranger.data.remote.domain.SingleUseCase
import org.rfcx.ranger.data.remote.domain.executor.PostExecutionThread
import org.rfcx.ranger.data.remote.domain.executor.ThreadExecutor
import org.rfcx.ranger.entity.event.ReviewEventFactory
import org.rfcx.ranger.entity.event.ReviewEventResponse

class ReviewEventUseCase(private val eventRepository: EventRepository, threadExecutor: ThreadExecutor,
                         postExecutionThread: PostExecutionThread) : SingleUseCase<ReviewEventFactory, Unit>(
		threadExecutor, postExecutionThread
) {
	override fun buildUseCaseObservable(params: ReviewEventFactory): Single<Unit> {
		return eventRepository.reviewEvent(params)
	}
}