package org.rfcx.incidents.data.remote.usertouch

import io.reactivex.Single

class UserTouchRepositoryImp(private val userTouchEndPoint: UserTouchEndPoint) : UserTouchRepository {
	override fun checkUser(): Single<Boolean> {
		
		return userTouchEndPoint.userTouch().map {
			it.success
		}
	}
}
