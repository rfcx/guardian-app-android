package org.rfcx.ranger.data.remote.response

import io.reactivex.Single
import okhttp3.ResponseBody
import retrofit2.http.Body
import retrofit2.http.POST
import java.util.*

interface CreateResponseEndpoint {
	@POST("responses")
	fun createResponse(@Body body: CreateResponseRequest): Single<ResponseBody>
}

data class CreateResponseRequest(
		val investigatedAt: String,
		val startedAt: String,
		val submittedAt: String,
		val evidences: List<Int>,
		val loggingScale: Int,
		val damageScale: Int,
		val responseActions: List<Int>,
		val note: String,
		val guardianId: String
)