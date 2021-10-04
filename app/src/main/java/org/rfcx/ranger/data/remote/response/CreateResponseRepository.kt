package org.rfcx.ranger.data.remote.response

import io.reactivex.Single
import okhttp3.ResponseBody

interface CreateResponseRepository {
	fun createResponseRequest(sendBody: CreateResponseRequest): Single<ResponseBody>
}
