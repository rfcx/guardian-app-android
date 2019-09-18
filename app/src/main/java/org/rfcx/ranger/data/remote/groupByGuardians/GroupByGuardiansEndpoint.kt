package org.rfcx.ranger.data.remote.groupByGuardians

import io.reactivex.Single
import org.rfcx.ranger.entity.guardian.GroupByGuardiansResponse
import retrofit2.http.GET
import retrofit2.http.Path

interface GroupByGuardiansEndpoint {
	@GET("guardians/group/{shortname}")
	fun sendShortName(@Path("shortname") shortname: String ): Single<GroupByGuardiansResponse>
}