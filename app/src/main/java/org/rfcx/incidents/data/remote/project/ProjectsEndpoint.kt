package org.rfcx.incidents.data.remote.project

import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ProjectsEndpoint {
    @GET("projects")
    fun getProjects(
        @Query("limit") limit: Int = 100,
        @Query("offset") offset: Int = 0,
        @Query("fields") fields: List<String> = listOf("id", "name", "permissions")
    ): Single<List<ProjectResponse>>

    @GET("projects/{id}/offtimes")
    fun getProjectOffTime(
        @Path("id") id: String
    ): Single<ProjectOffTimeResponse>
}
