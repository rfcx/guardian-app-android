package org.rfcx.incidents.data

import io.reactivex.Single
import org.rfcx.incidents.data.interfaces.UnsubscribeRepository
import org.rfcx.incidents.data.remote.subscribe.SubscribeEndpoint
import org.rfcx.incidents.entity.SubscribeRequest
import org.rfcx.incidents.entity.SubscribeResponse

class UnsubscribeRepositoryImp(private val subscribeEndpoint: SubscribeEndpoint) : UnsubscribeRepository {
    override fun sendUnsubscribeBody(sendBody: SubscribeRequest): Single<SubscribeResponse> {
        return subscribeEndpoint.sendGroupsUnsubscribe(sendBody)
    }
}
