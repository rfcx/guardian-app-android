package org.rfcx.incidents.data.remote.domain.classified

import io.reactivex.Single
import org.rfcx.incidents.data.remote.data.classified.ClassifiedRepository
import org.rfcx.incidents.data.remote.domain.SingleUseCase
import org.rfcx.incidents.data.remote.domain.executor.PostExecutionThread
import org.rfcx.incidents.data.remote.domain.executor.ThreadExecutor
import org.rfcx.incidents.entity.event.ClassificationBody
import org.rfcx.incidents.entity.event.Confidence

class GetClassifiedUseCase(private val classifiedRepository: ClassifiedRepository, threadExecutor: ThreadExecutor,
                           postExecutionThread: PostExecutionThread) : SingleUseCase<ClassificationBody, List<Confidence>>(threadExecutor, postExecutionThread) {
	
	override fun buildUseCaseObservable(params: ClassificationBody): Single<List<Confidence>> {
		return classifiedRepository.getClassifiedCation(params)
	}
}
