package org.rfcx.incidents.data

import io.reactivex.Single
import org.rfcx.incidents.entity.SubscribeRequest
import org.rfcx.incidents.entity.SubscribeResponse

interface UnsubscribeRepository {
    fun sendUnsubscribeBody(sendBody: SubscribeRequest): Single<SubscribeResponse>
}
