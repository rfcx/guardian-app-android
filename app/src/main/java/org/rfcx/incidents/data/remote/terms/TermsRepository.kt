package org.rfcx.incidents.data.remote.terms

import io.reactivex.Single
import org.rfcx.incidents.entity.terms.TermsRequest
import org.rfcx.incidents.entity.terms.TermsResponse

interface TermsRepository {
	fun sendBodyPayload(sendBody: TermsRequest): Single<TermsResponse>
}
