package org.rfcx.incidents.data.remote.streams

import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Query

interface StreamEndpoint {

    @GET("streams")
    fun getStreams(
        @Query("limit") limit: Int = 100,
        @Query("offset") offset: Int = 0,
        @Query("updated_after", encoded = true) updatedAfter: String? = null,
        @Query("sort", encoded = true) sort: String? = null,
        @Query("projects") projects: List<String>? = null,
        @Query("type") type: String = "guardian"
    ): Single<List<StreamDeviceAPIResponse>>
}
