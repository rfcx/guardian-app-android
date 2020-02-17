package org.rfcx.ranger.data.remote.subscribe

import io.reactivex.Single
import org.rfcx.ranger.entity.SubscribeRequest
import org.rfcx.ranger.entity.SubscribeResponse

interface SubscribeRepository {
	fun sendBody(sendBody: SubscribeRequest): Single<SubscribeResponse>
}