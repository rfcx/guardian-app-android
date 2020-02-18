package org.rfcx.ranger.data.remote.subscribe.unsubscribe

import io.reactivex.Single
import org.rfcx.ranger.data.remote.subscribe.SubscribeEndpoint
import org.rfcx.ranger.entity.SubscribeRequest
import org.rfcx.ranger.entity.SubscribeResponse

class UnsubscribeRepositoryImp(private val subscribeEndpoint: SubscribeEndpoint) : UnsubscribeRepository {
	override fun sendUnsubscribeBody(sendBody: SubscribeRequest): Single<SubscribeResponse> {
		return subscribeEndpoint.sendGroupsUnsubscribe(sendBody)
	}
}