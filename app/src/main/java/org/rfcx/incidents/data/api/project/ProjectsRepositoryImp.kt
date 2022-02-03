package org.rfcx.incidents.data.api.project

import io.reactivex.Single
import org.rfcx.incidents.data.local.CachedEndpointDb
import org.rfcx.incidents.data.local.ProjectDb
import org.rfcx.incidents.entity.project.Project

class ProjectsRepositoryImp(
    private val endpoint: ProjectsEndpoint,
    private val projectDb: ProjectDb,
    private val cachedEndpointDb: CachedEndpointDb
) : ProjectsRepository {

    override fun getProjects(options: GetProjectsOptions): Single<List<Project>> {
        if (options.forceRefresh || cachedEndpointDb.hasCachedEndpoint("GetProjects")) {
            return refreshFromAPI()
        }
        return getFromLocalDB()
    }

    private fun refreshFromAPI(): Single<List<Project>> {
        return endpoint.getProjects().doAfterSuccess { rawProjects ->
            rawProjects.forEach {
                projectDb.insertOrUpdate(it)
            }
        }.to { getFromLocalDB() }
    }

    private fun getFromLocalDB(): Single<List<Project>> {
        return Single.just(projectDb.getProjects())
    }
}
