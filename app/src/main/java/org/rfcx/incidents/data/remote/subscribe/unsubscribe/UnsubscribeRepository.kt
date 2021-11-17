package org.rfcx.incidents.data.remote.subscribe.unsubscribe

import io.reactivex.Single
import org.rfcx.incidents.entity.SubscribeRequest
import org.rfcx.incidents.entity.SubscribeResponse

interface UnsubscribeRepository {
	fun sendUnsubscribeBody(sendBody: SubscribeRequest): Single<SubscribeResponse>
}
