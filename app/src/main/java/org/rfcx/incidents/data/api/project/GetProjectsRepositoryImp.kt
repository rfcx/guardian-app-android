package org.rfcx.incidents.data.api.project

import io.reactivex.Single

class GetProjectsRepositoryImp(private val endpoint: GetProjectsEndpoint) : GetProjectsRepository {
    override fun getProjects(requestFactory: ProjectsRequestFactory): Single<List<ProjectResponse>> {
        return endpoint.getProjects(requestFactory.limit, requestFactory.offset, requestFactory.fields)
    }
}
