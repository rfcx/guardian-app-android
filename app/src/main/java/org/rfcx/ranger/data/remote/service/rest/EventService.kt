package org.rfcx.ranger.data.remote.service.rest

import io.reactivex.Single
import org.rfcx.ranger.entity.event.EventResponse
import org.rfcx.ranger.entity.event.ReviewEventRequest
import org.rfcx.ranger.entity.event.ReviewEventResponse
import retrofit2.Call
import retrofit2.http.*

interface EventService {
	@GET("v2/events")
	fun getEvents(@Query("limit") limit: Int,
	              @Query("offset") offset: Int,
	              @Query("order") orderBy: String,
	              @Query("dir") dir: String,
	              @Query("guardian_groups[]") guardianGroup: List<String>): Single<EventResponse>
	
	@GET("v2/events") // load see older
	fun getEventsGuardian(@Query("guardians[]") guardian: String,
	                      @Query("values[]") value: String,
	                      @Query("starting_before_local") ending: String,
	                      @Query("order") orderBy: String,
	                      @Query("dir") dir: String,
	                      @Query("limit") limit: Int,
	                      @Query("offset") offset: Int): Single<EventResponse>
	
	@POST("v2/events/{event_guid}/review")
	fun reviewEvent(@Path("event_guid") eventGuID: String,
	                @Body body: ReviewEventRequest)
			: Call<ReviewEventResponse>
}