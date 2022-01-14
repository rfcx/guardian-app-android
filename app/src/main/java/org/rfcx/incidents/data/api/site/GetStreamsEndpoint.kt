package org.rfcx.incidents.data.api.site

import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Query

interface GetStreamsEndpoint {
	@GET("streams")
	fun getStreams(@Query("limit") limit: Int = 20,
	               @Query("offset") offset: Int = 0,
	               @Query("with_events_count") withEventsCount: Boolean = true,
	               @Query("updated_after", encoded = true) updatedAfter: String? = null,
	               @Query("sort", encoded = true) sort: String? = null,
	               @Query("projects") projects: List<String>? = null): Single<List<StreamResponse>>
}
