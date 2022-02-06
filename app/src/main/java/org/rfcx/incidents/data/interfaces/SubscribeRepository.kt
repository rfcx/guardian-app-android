package org.rfcx.incidents.data.interfaces

import io.reactivex.Single
import org.rfcx.incidents.entity.user.SubscribeRequest
import org.rfcx.incidents.entity.user.SubscribeResponse

interface SubscribeRepository {
    fun subscribe(request: SubscribeRequest): Single<SubscribeResponse>
    fun unsubscribe(request: SubscribeRequest): Single<SubscribeResponse>
}
