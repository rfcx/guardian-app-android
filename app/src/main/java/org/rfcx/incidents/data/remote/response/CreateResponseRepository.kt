package org.rfcx.incidents.data.remote.response

import io.reactivex.Single

interface CreateResponseRepository {
	fun createResponseRequest(sendBody: CreateResponseRequest): Single<CreateResponseRes>
}
