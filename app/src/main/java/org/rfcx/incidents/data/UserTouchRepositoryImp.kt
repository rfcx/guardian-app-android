package org.rfcx.incidents.data

import io.reactivex.Single
import org.rfcx.incidents.data.remote.usertouch.UserTouchEndPoint

class UserTouchRepositoryImp(private val userTouchEndPoint: UserTouchEndPoint) : UserTouchRepository {
    override fun checkUser(): Single<Boolean> {

        return userTouchEndPoint.userTouch().map {
            it.success
        }
    }
}
