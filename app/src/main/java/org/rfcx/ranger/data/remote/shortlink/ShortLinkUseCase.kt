package org.rfcx.ranger.data.remote.shortlink

import io.reactivex.Single
import org.rfcx.ranger.data.remote.domain.SingleUseCase
import org.rfcx.ranger.data.remote.domain.executor.PostExecutionThread
import org.rfcx.ranger.data.remote.domain.executor.ThreadExecutor
import org.rfcx.ranger.entity.shortlink.ShortLinkRequest

class ShortLinkUseCase(private val shortLinkRepository: ShortLinkRepository,
                       threadExecutor: ThreadExecutor, postExecutionThread: PostExecutionThread
) : SingleUseCase<ShortLinkRequest, String>(threadExecutor, postExecutionThread) {
	override fun buildUseCaseObservable(params: ShortLinkRequest): Single<String> {
		return shortLinkRepository.sendShortLinkRequest(params)
	}
}