package org.rfcx.incidents.data.remote.usertouch

import io.reactivex.Single
import org.rfcx.incidents.entity.user.UserTouchResponse
import retrofit2.http.GET

interface UserTouchEndPoint {
    
    @GET("v1/users/touchapi")
    fun userTouch(): Single<UserTouchResponse>
}
