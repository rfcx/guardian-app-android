package org.rfcx.incidents.data.remote.subscribe

import io.reactivex.Single
import org.rfcx.incidents.entity.SubscribeRequest
import org.rfcx.incidents.entity.SubscribeResponse

class SubscribeRepositoryImp(private val subscribeEndpoint: SubscribeEndpoint) : SubscribeRepository {
    override fun sendBody(sendBody: SubscribeRequest): Single<SubscribeResponse> {
        return subscribeEndpoint.sendGroups(sendBody)
    }
}
