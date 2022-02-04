package org.rfcx.incidents.data

import io.reactivex.Single
import org.rfcx.incidents.data.remote.response.CreateResponseRequest
import org.rfcx.incidents.data.remote.response.CreateResponseRes

interface CreateResponseRepository {
    fun createResponseRequest(sendBody: CreateResponseRequest): Single<CreateResponseRes>
}
