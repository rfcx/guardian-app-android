package org.rfcx.incidents.data.interfaces

import io.reactivex.Single

interface UserTouchRepository {

    fun checkUser(): Single<Boolean>
}
