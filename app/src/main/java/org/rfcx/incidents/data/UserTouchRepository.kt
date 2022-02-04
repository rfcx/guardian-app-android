package org.rfcx.incidents.data

import io.reactivex.Single

interface UserTouchRepository {

    fun checkUser(): Single<Boolean>
}
