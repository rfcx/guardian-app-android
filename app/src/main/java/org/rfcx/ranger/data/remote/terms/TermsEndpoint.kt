package org.rfcx.ranger.data.remote.terms

import io.reactivex.Single
import org.rfcx.ranger.entity.terms.TermsRequest
import org.rfcx.ranger.entity.terms.TermsResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface TermsEndpoint {
	
	@POST("v1/users/accept-terms")
	fun sendPayload(@Body body: TermsRequest): Single<TermsResponse>
	
}