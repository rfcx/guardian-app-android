package org.rfcx.ranger.data.remote.subscribe

import io.reactivex.Single
import org.rfcx.ranger.entity.SubscribeRequest
import org.rfcx.ranger.entity.SubscribeResponse

class SubscribeRepositoryImp (private val subscribeEndpoint: SubscribeEndpoint) : SubscribeRepository {
	override fun sendBody(sendBody: SubscribeRequest): Single<SubscribeResponse> {
		return subscribeEndpoint.sendGroups(sendBody)
	}
}