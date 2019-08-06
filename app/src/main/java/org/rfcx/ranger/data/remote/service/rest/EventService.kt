package org.rfcx.ranger.data.remote.service.rest

import io.reactivex.Single
import org.rfcx.ranger.entity.event.EventResponse
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface EventService {
	@GET("events/event/datatable")
	fun getEvents(@Header("Authorization") authUser: String,
	              @Query("guardian_groups[]") guardianGroup: String,
	              @Query("order") orderBy: String,
	              @Query("dir") dir: String,
	              @Query("limit") limit: Int,
	              @Query("offset") offset: Int): Single<EventResponse>
}