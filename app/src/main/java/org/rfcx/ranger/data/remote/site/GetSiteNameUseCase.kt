package org.rfcx.ranger.data.remote.site

import io.reactivex.Single
import org.rfcx.ranger.data.remote.domain.SingleUseCase
import org.rfcx.ranger.data.remote.domain.executor.PostExecutionThread
import org.rfcx.ranger.data.remote.domain.executor.ThreadExecutor
import org.rfcx.ranger.entity.site.SiteResponse

class GetSiteNameUseCase(private val siteRepository: SiteRepository,
                         threadExecutor: ThreadExecutor, postExecutionThread: PostExecutionThread) : SingleUseCase<String, List<SiteResponse>>(threadExecutor, postExecutionThread) {
	override fun buildUseCaseObservable(params: String): Single<List<SiteResponse>> {
		return siteRepository.site(params)
	}
}