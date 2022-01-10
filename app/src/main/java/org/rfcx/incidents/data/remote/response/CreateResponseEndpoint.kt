package org.rfcx.incidents.data.remote.response

import io.reactivex.Single
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface CreateResponseEndpoint {
	@POST("responses")
	fun createResponse(@Body body: CreateResponseRequest): Single<CreateResponseRes>
	
	@POST("responses")
	fun createNewResponse(@Body body: CreateResponseRequest): Call<CreateResponseRes>
}

data class CreateResponseRequest(
		val investigatedAt: String,
		val startedAt: String,
		val submittedAt: String,
		val items : List<Int>,
		val note: String?,
		val streamId: String
)

open class CreateResponseRes(
		val incidentRef: String
)
