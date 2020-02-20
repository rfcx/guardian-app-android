package org.rfcx.ranger.data.remote.subscribe.unsubscribe

import io.reactivex.Single
import org.rfcx.ranger.entity.SubscribeRequest
import org.rfcx.ranger.entity.SubscribeResponse

interface UnsubscribeRepository {
	fun sendUnsubscribeBody(sendBody: SubscribeRequest): Single<SubscribeResponse>
}