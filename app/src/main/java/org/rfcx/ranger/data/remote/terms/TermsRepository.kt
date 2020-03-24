package org.rfcx.ranger.data.remote.terms

import io.reactivex.Single
import org.rfcx.ranger.entity.terms.TermsRequest
import org.rfcx.ranger.entity.terms.TermsResponse

interface TermsRepository {
	fun sendBodyPayload(sendBody: TermsRequest): Single<TermsResponse>
}