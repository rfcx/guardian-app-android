package org.rfcx.incidents.data.remote.setusername

import io.reactivex.Single
import org.rfcx.incidents.entity.user.SetNameRequest
import org.rfcx.incidents.entity.user.SetNameResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface SetNameEndpoint {
    @POST("v1/users/auth0/update-user/public")
    fun sendGivenName(@Body body: SetNameRequest): Single<SetNameResponse>
}
