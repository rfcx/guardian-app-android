package org.rfcx.ranger.data.remote.service.rest

import io.reactivex.Single
import org.rfcx.ranger.entity.event.EventResponse
import org.rfcx.ranger.entity.event.ReviewEventResponse
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface EventService {
	@GET("events/event/datatable")
	fun getEvents(@Query("guardian_groups[]") guardianGroup: String,
	              @Query("order") orderBy: String,
	              @Query("dir") dir: String,
	              @Query("limit") limit: Int,
	              @Query("offset") offset: Int): Single<EventResponse>
	
	@GET("events/event/datatable")
	fun getEventsGuardian(@Query("guardians[]") guardianGroup: List<String>,
	                      @Query("order") orderBy: String,
	                      @Query("dir") dir: String,
	                      @Query("limit") limit: Int,
	                      @Query("offset") offset: Int): Single<EventResponse>
	
	@POST("events/{event_guid}/{review_confirmed}")
	fun reviewEvent(@Path("event_guid") eventGuID: String,
	                @Path("review_confirmed") reviewConfirm: String)
			: Single<ReviewEventResponse>
	
}