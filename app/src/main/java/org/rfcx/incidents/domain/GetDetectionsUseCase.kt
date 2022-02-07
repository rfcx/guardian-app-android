package org.rfcx.incidents.domain

import io.reactivex.Single
import org.rfcx.incidents.data.interfaces.DetectionsRepository
import org.rfcx.incidents.domain.base.SingleUseCase
import org.rfcx.incidents.domain.executor.PostExecutionThread
import org.rfcx.incidents.domain.executor.ThreadExecutor
import org.rfcx.incidents.entity.event.DetectionFactory
import org.rfcx.incidents.entity.event.Detections

class GetDetectionsUseCase(
    private val repository: DetectionsRepository,
    threadExecutor: ThreadExecutor,
    postExecutionThread: PostExecutionThread
) : SingleUseCase<DetectionFactory, List<Detections>>(threadExecutor, postExecutionThread) {
    override fun buildUseCaseObservable(params: DetectionFactory): Single<List<Detections>> {
        return repository.getDetections(params)
    }
}
