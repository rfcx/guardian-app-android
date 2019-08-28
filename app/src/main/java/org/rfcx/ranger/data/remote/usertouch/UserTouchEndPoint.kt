package org.rfcx.ranger.data.remote.usertouch

import io.reactivex.Single
import org.rfcx.ranger.entity.user.UserTouchResponse
import retrofit2.http.GET

interface UserTouchEndPoint {
	
	@GET("users/touchapi")
	fun userTouch(): Single<UserTouchResponse>
}