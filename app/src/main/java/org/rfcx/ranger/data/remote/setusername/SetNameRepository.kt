package org.rfcx.ranger.data.remote.setusername

import io.reactivex.Single
import org.rfcx.ranger.entity.user.SetNameRequest
import org.rfcx.ranger.entity.user.SetNameResponse

interface SetNameRepository {
	fun sendName(sendBody: SetNameRequest): Single<SetNameResponse>
}