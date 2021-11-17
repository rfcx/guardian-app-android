package org.rfcx.incidents.data.remote.guardianGroup

import io.reactivex.Single
import org.rfcx.incidents.entity.guardian.GuardianGroup
import retrofit2.http.GET

interface GuardianGroupEndpoint {
	@GET("v1/guardians/groups")
	fun guardianGroups(): Single<List<GuardianGroup>>
}
