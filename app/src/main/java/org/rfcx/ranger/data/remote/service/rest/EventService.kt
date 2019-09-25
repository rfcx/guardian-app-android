package org.rfcx.ranger.data.remote.service.rest

import io.reactivex.Single
import org.rfcx.ranger.entity.event.EventResponse
import org.rfcx.ranger.entity.event.ReviewEventResponse
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface EventService {
	@GET("v2/events")
	fun getEvents(@Query("limit") limit: Int,
	              @Query("offset") offset: Int,
	              @Query("order") orderBy: String,
	              @Query("dir") dir: String,
	              @Query("guardian[]") guardianGroup: List<String>): Single<EventResponse>
	
	@POST("v1/events/{event_guid}/{review_confirmed}")
	fun reviewEvent(@Path("event_guid") eventGuID: String,
	                @Path("review_confirmed") reviewConfirm: String)
			: Single<ReviewEventResponse>
}