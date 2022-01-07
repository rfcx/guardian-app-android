package org.rfcx.incidents.data.api.incident

import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Query

interface IncidentEndpoint {
	@GET("streams/incidents")
	fun getIncident(@Query("limit_incidents") limit: Int = 1,
	                @Query("keyword") keyword: String = ""): Single<List<IncidentsResponse>>
}
