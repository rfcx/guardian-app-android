package org.rfcx.incidents.data

import io.reactivex.Single
import org.rfcx.incidents.data.interfaces.CreateResponseRepository
import org.rfcx.incidents.data.remote.response.CreateResponseEndpoint
import org.rfcx.incidents.data.remote.response.CreateResponseRequest
import org.rfcx.incidents.data.remote.response.CreateResponseRes

class CreateResponseRepositoryImpl(private val endpoint: CreateResponseEndpoint) : CreateResponseRepository {
    override fun createResponseRequest(sendBody: CreateResponseRequest): Single<CreateResponseRes> {
        return endpoint.createResponse(sendBody)
    }
}
