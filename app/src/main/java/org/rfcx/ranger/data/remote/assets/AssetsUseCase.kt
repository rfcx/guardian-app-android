package org.rfcx.ranger.data.remote.assets

import io.reactivex.Single
import okhttp3.ResponseBody
import org.rfcx.ranger.data.remote.domain.SingleUseCase
import org.rfcx.ranger.data.remote.domain.executor.PostExecutionThread
import org.rfcx.ranger.data.remote.domain.executor.ThreadExecutor

class AssetsUseCase(private val assetsRepository: AssetsRepository,
                    threadExecutor: ThreadExecutor, postExecutionThread: PostExecutionThread) : SingleUseCase<String, ResponseBody>(threadExecutor, postExecutionThread) {
	override fun buildUseCaseObservable(params: String): Single<ResponseBody> {
		return assetsRepository.sendFilename(params)
	}
}
