package org.rfcx.incidents.domain

import io.reactivex.Single
import okhttp3.ResponseBody
import org.rfcx.incidents.data.interfaces.MediaRepository
import org.rfcx.incidents.domain.base.SingleUseCase
import org.rfcx.incidents.domain.executor.PostExecutionThread
import org.rfcx.incidents.domain.executor.ThreadExecutor

class MediaUseCase(
    private val repository: MediaRepository,
    threadExecutor: ThreadExecutor,
    postExecutionThread: PostExecutionThread
) : SingleUseCase<String, ResponseBody>(threadExecutor, postExecutionThread) {
    override fun buildUseCaseObservable(params: String): Single<ResponseBody> {
        return repository.sendFilename(params)
    }
}
