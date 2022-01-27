package org.rfcx.incidents.data.api.detections

import io.reactivex.Single
import org.rfcx.incidents.entity.alert.Detections
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface DetectionsEndpoint {
    @GET("streams/{id}/detections")
    fun getDetections(
        @Path("id") streamId: String,
        @Query("start") start: Long = 0,
        @Query("end") end: Long = 0,
        @Query("classifications") classifications: List<String> = listOf()
    ): Single<List<Detections>>
}
