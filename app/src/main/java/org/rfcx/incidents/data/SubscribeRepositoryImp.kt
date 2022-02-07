package org.rfcx.incidents.data

import io.reactivex.Single
import org.rfcx.incidents.data.interfaces.SubscribeRepository
import org.rfcx.incidents.data.remote.subscribe.SubscribeEndpoint
import org.rfcx.incidents.entity.user.SubscribeRequest
import org.rfcx.incidents.entity.user.SubscribeResponse

class SubscribeRepositoryImp(private val subscribeEndpoint: SubscribeEndpoint) : SubscribeRepository {
    override fun subscribe(request: SubscribeRequest): Single<SubscribeResponse> {
        return subscribeEndpoint.sendGroups(request)
    }

    override fun unsubscribe(request: SubscribeRequest): Single<SubscribeResponse> {
        return subscribeEndpoint.sendGroupsUnsubscribe(request)
    }
}
