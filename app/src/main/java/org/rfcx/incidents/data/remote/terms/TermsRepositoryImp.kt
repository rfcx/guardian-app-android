package org.rfcx.incidents.data.remote.terms

import io.reactivex.Single
import org.rfcx.incidents.entity.terms.TermsRequest
import org.rfcx.incidents.entity.terms.TermsResponse

class TermsRepositoryImp(private val termsEndpoint: TermsEndpoint) : TermsRepository {
    override fun sendBodyPayload(sendBody: TermsRequest): Single<TermsResponse> {
        return termsEndpoint.sendPayload(sendBody)
    }
}
