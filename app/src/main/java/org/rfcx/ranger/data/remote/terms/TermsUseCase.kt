package org.rfcx.ranger.data.remote.terms

import io.reactivex.Single
import org.rfcx.ranger.data.remote.domain.SingleUseCase
import org.rfcx.ranger.data.remote.domain.executor.PostExecutionThread
import org.rfcx.ranger.data.remote.domain.executor.ThreadExecutor
import org.rfcx.ranger.entity.terms.TermsRequest
import org.rfcx.ranger.entity.terms.TermsResponse

class TermsUseCase(private val termsRepository: TermsRepository,
                   threadExecutor: ThreadExecutor, postExecutionThread: PostExecutionThread
) : SingleUseCase<TermsRequest, TermsResponse>(threadExecutor, postExecutionThread) {
	override fun buildUseCaseObservable(params: TermsRequest): Single<TermsResponse> {
		return termsRepository.sendBodyPayload(params)
	}
}