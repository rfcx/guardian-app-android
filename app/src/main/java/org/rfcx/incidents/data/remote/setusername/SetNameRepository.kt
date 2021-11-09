package org.rfcx.incidents.data.remote.setusername

import io.reactivex.Single
import org.rfcx.incidents.entity.user.SetNameRequest
import org.rfcx.incidents.entity.user.SetNameResponse

interface SetNameRepository {
	fun sendName(sendBody: SetNameRequest): Single<SetNameResponse>
}
