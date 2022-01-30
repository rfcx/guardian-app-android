package org.rfcx.incidents.data.remote.subscribe

import io.reactivex.Single
import org.rfcx.incidents.entity.SubscribeRequest
import org.rfcx.incidents.entity.SubscribeResponse

interface SubscribeRepository {
    fun sendBody(sendBody: SubscribeRequest): Single<SubscribeResponse>
}
