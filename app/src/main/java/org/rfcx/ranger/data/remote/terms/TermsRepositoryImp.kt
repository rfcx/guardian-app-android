package org.rfcx.ranger.data.remote.terms

import io.reactivex.Single
import org.rfcx.ranger.entity.terms.TermsRequest
import org.rfcx.ranger.entity.terms.TermsResponse

class TermsRepositoryImp(private val termsEndpoint: TermsEndpoint) : TermsRepository {
	
	override fun sendBodyPayload(sendBody: TermsRequest): Single<TermsResponse> {
		return termsEndpoint.sendPayload(sendBody)
	}
	
}