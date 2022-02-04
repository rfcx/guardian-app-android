package org.rfcx.incidents.data.interfaces

import io.reactivex.Single
import org.rfcx.incidents.entity.SubscribeRequest
import org.rfcx.incidents.entity.SubscribeResponse

interface SubscribeRepository {
    fun sendBody(sendBody: SubscribeRequest): Single<SubscribeResponse>
}
