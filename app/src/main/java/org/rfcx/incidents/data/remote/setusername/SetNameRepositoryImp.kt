package org.rfcx.incidents.data.remote.setusername

import io.reactivex.Single
import org.rfcx.incidents.entity.user.SetNameRequest
import org.rfcx.incidents.entity.user.SetNameResponse

class SetNameRepositoryImp(private val setNameEndpoint: SetNameEndpoint) : SetNameRepository {
	override fun sendName(sendBody: SetNameRequest): Single<SetNameResponse> {
		return setNameEndpoint.sendGivenName(sendBody)
	}
}
