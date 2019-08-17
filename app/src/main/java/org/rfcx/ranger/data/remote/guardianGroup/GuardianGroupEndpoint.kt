package org.rfcx.ranger.data.remote.guardianGroup

import io.reactivex.Single
import org.rfcx.ranger.entity.guardian.GuardianGroup
import retrofit2.http.GET

interface GuardianGroupEndpoint {
	@GET("guardians/groups")
	fun guardianGroups(): Single<List<GuardianGroup>>
}