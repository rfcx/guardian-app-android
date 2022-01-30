package org.rfcx.incidents.data.remote.terms

import io.reactivex.Single
import org.rfcx.incidents.data.remote.domain.SingleUseCase
import org.rfcx.incidents.data.remote.domain.executor.PostExecutionThread
import org.rfcx.incidents.data.remote.domain.executor.ThreadExecutor
import org.rfcx.incidents.entity.terms.TermsRequest
import org.rfcx.incidents.entity.terms.TermsResponse

class TermsUseCase(
    private val termsRepository: TermsRepository,
    threadExecutor: ThreadExecutor, postExecutionThread: PostExecutionThread
) : SingleUseCase<TermsRequest, TermsResponse>(threadExecutor, postExecutionThread) {
    override fun buildUseCaseObservable(params: TermsRequest): Single<TermsResponse> {
        return termsRepository.sendBodyPayload(params)
    }
}
