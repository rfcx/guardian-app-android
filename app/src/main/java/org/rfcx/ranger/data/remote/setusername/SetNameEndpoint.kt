package org.rfcx.ranger.data.remote.setusername

import io.reactivex.Single
import org.rfcx.ranger.entity.user.SetNameRequest
import org.rfcx.ranger.entity.user.SetNameResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface SetNameEndpoint {
	@POST("users/auth0/update-user/public")
	fun sendGivenName(@Body body: SetNameRequest): Single<SetNameResponse>
}

