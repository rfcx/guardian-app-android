package org.rfcx.incidents.data.remote.subscribe

import io.reactivex.Single
import org.rfcx.incidents.entity.SubscribeRequest
import org.rfcx.incidents.entity.SubscribeResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface SubscribeEndpoint {
    @POST("v1/guardians/groups/subscribe")
    fun sendGroups(@Body body: SubscribeRequest): Single<SubscribeResponse>
    
    @POST("v1/guardians/groups/unsubscribe")
    fun sendGroupsUnsubscribe(@Body body: SubscribeRequest): Single<SubscribeResponse>
}
