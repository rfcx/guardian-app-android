package org.rfcx.incidents.data.remote.response

import io.reactivex.Single

class CreateResponseRepositoryImp(private val endpoint: CreateResponseEndpoint) : CreateResponseRepository {
    override fun createResponseRequest(sendBody: CreateResponseRequest): Single<CreateResponseRes> {
        return endpoint.createResponse(sendBody)
    }
}
