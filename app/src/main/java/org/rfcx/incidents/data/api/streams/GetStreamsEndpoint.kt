package org.rfcx.incidents.data.api.streams

import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Query

interface GetStreamsEndpoint {
    @GET("streams")
    fun getStreams(
        @Query("limit") limit: Int = 10,
        @Query("offset") offset: Int = 0,
        @Query("limit_incidents") limitIncidents: Int = 1,
        @Query("projects") projects: List<String>? = null
    ): Single<List<StreamResponse>>
}
