package org.rfcx.incidents.data.remote.site

import io.reactivex.Single
import org.rfcx.incidents.data.remote.domain.SingleUseCase
import org.rfcx.incidents.data.remote.domain.executor.PostExecutionThread
import org.rfcx.incidents.data.remote.domain.executor.ThreadExecutor
import org.rfcx.incidents.entity.site.SiteResponse

class GetSiteNameUseCase(
    private val siteRepository: SiteRepository,
    threadExecutor: ThreadExecutor,
    postExecutionThread: PostExecutionThread
) : SingleUseCase<String, List<SiteResponse>>(threadExecutor, postExecutionThread) {
    override fun buildUseCaseObservable(params: String): Single<List<SiteResponse>> {
        return siteRepository.site(params)
    }
}
