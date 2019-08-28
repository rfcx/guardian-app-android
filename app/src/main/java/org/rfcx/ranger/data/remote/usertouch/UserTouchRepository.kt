package org.rfcx.ranger.data.remote.usertouch

import io.reactivex.Single

interface UserTouchRepository {
	
	fun checkUser(): Single<Boolean>
}