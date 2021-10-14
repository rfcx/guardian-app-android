package org.rfcx.ranger.data.remote.response

import io.reactivex.Single
import okhttp3.ResponseBody

class CreateResponseRepositoryImp(private val endpoint: CreateResponseEndpoint) : CreateResponseRepository {
	override fun createResponseRequest(sendBody: CreateResponseRequest): Single<CreateResponseRes> {
		return endpoint.createResponse(sendBody)
	}
}
