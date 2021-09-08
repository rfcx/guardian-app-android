package org.rfcx.ranger.data.api.project

import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Query

interface GetProjectsEndpoint {
	@GET("projects")
	fun getProjects(
			@Query("limit") limit: Int = 100,
			@Query("offset") offset: Int = 0,
			@Query("fields") fields: List<String> = listOf("id", "name", "permissions")
	): Single<List<ProjectResponse>>
}
