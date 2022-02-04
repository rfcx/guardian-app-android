package org.rfcx.incidents.data.remote.detections

import io.reactivex.Single
import org.rfcx.incidents.domain.SingleUseCase
import org.rfcx.incidents.domain.executor.PostExecutionThread
import org.rfcx.incidents.domain.executor.ThreadExecutor
import org.rfcx.incidents.entity.alert.DetectionFactory
import org.rfcx.incidents.entity.alert.Detections

class GetDetections(
    private val repository: DetectionsRepository,
    threadExecutor: ThreadExecutor,
    postExecutionThread: PostExecutionThread
) : SingleUseCase<DetectionFactory, List<Detections>>(threadExecutor, postExecutionThread) {
    override fun buildUseCaseObservable(params: DetectionFactory): Single<List<Detections>> {
        return repository.getDetections(params)
    }
}
