package org.rfcx.incidents.data.remote.terms

import io.reactivex.Single
import org.rfcx.incidents.entity.terms.TermsRequest
import org.rfcx.incidents.entity.terms.TermsResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface TermsEndpoint {
	@POST("v1/users/accept-terms")
	fun sendPayload(@Body body: TermsRequest): Single<TermsResponse>
}
