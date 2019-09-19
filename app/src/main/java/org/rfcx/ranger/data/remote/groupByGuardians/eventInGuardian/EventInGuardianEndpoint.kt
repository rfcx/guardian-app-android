package org.rfcx.ranger.data.remote.groupByGuardians.eventInGuardian

import io.reactivex.Single
import org.rfcx.ranger.entity.event.EventInGuardianResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface EventInGuardianEndpoint {
	@GET("events/event/datatable")
	fun sendGuardianName(@Query("guardians[]") guardianGroup: String,
	                     @Query("limit") limit: Int = 50,
	                     @Query("offset") offset: Int = 0): Single<EventInGuardianResponse>
}
