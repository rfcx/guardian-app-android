package org.rfcx.incidents.data.api.detections

import io.reactivex.Single
import org.rfcx.incidents.data.remote.domain.SingleUseCase
import org.rfcx.incidents.data.remote.domain.executor.PostExecutionThread
import org.rfcx.incidents.data.remote.domain.executor.ThreadExecutor
import org.rfcx.incidents.entity.alert.DetectionFactory
import org.rfcx.incidents.entity.alert.Detections

class GetDetections(
    private val repository: DetectionsRepository,
    threadExecutor: ThreadExecutor, postExecutionThread: PostExecutionThread
) : SingleUseCase<DetectionFactory, List<Detections>>(threadExecutor, postExecutionThread) {
    override fun buildUseCaseObservable(params: DetectionFactory): Single<List<Detections>> {
        return repository.getDetections(params)
    }
}
