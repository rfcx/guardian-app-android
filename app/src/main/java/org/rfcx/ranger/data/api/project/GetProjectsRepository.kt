package org.rfcx.ranger.data.api.project

import io.reactivex.Single

interface GetProjectsRepository {
	fun getProjects(requestFactory: ProjectsRequestFactory): Single<List<ProjectResponse>>
}