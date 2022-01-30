package org.rfcx.incidents.data.remote.usertouch

import io.reactivex.Single

interface UserTouchRepository {

    fun checkUser(): Single<Boolean>
}
