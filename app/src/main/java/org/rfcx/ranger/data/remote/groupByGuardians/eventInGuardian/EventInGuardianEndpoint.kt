package org.rfcx.ranger.data.remote.groupByGuardians.eventInGuardian

import io.reactivex.Single
import org.rfcx.ranger.entity.event.EventInGuardianResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface EventInGuardianEndpoint {
	@GET("events/event/datatable")
	fun sendGuardianName(@Query("guardians[]") guardianGroup: String): Single<EventInGuardianResponse>
}
