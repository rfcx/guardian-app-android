package org.rfcx.ranger.data.remote.service.rest

import io.reactivex.Single
import org.rfcx.ranger.entity.event.EventResponse
import org.rfcx.ranger.entity.event.EventsResponse
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
	              @Query("guardian_groups[]") guardianGroup: List<String>,
	              @Query("values[]") value: List<String>): Single<EventsResponse>
	
	@GET("v2/events") // load see older
	fun getEventsGuardian(@Query("guardians[]") guardian: String,
	                      @Query("values[]") value: String,
	                      @Query("order") orderBy: String,
	                      @Query("dir") dir: String,
	                      @Query("limit") limit: Int,
	                      @Query("offset") offset: Int): Single<EventsResponse>
	
	@POST("v2/events/{id}/review")
	fun reviewEvent(@Path("id") eventGuID: String,
	                @Body body: ReviewEventRequest)
			: Call<ReviewEventResponse>
	
	@POST("v2/events/{id}/review")
	fun reviewEventOnline(@Path("id") eventGuID: String,
	                @Body body: ReviewEventRequest)
			: Single<ReviewEventResponse>
	
	@GET("v2/events/{event_guid}")
	fun getEvent(@Path("event_guid") eventGuID: String)
			: Single<EventResponse>
	
	
}