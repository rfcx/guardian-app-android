package org.rfcx.incidents.data.api.media

import io.reactivex.Single
import okhttp3.ResponseBody
import org.rfcx.incidents.data.remote.domain.SingleUseCase
import org.rfcx.incidents.data.remote.domain.executor.PostExecutionThread
import org.rfcx.incidents.data.remote.domain.executor.ThreadExecutor

class MediaUseCase(private val repository: MediaRepository,
                   threadExecutor: ThreadExecutor, postExecutionThread: PostExecutionThread) : SingleUseCase<String, ResponseBody>(threadExecutor, postExecutionThread) {
	override fun buildUseCaseObservable(params: String): Single<ResponseBody> {
		return repository.sendFilename(params)
	}
}