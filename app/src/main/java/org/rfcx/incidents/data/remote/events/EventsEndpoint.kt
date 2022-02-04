package org.rfcx.incidents.data.remote.events

import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Path

interface EventsEndpoint {
    @GET("streams/{id}/last-events")
    fun getProjects(@Path("id") streamId: String): Single<List<ResponseEvent>>
}
