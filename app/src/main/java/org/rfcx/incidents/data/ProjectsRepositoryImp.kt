package org.rfcx.incidents.data

import io.reactivex.Single
import kotlinx.coroutines.flow.Flow
import org.rfcx.incidents.data.interfaces.ProjectsRepository
import org.rfcx.incidents.data.local.CachedEndpointDb
import org.rfcx.incidents.data.local.ProjectDb
import org.rfcx.incidents.data.remote.project.ProjectsEndpoint
import org.rfcx.incidents.domain.executor.PostExecutionThread
import org.rfcx.incidents.entity.stream.Project
import org.rfcx.incidents.util.ConnectivityUtils

class ProjectsRepositoryImp(
    private val endpoint: ProjectsEndpoint,
    private val projectDb: ProjectDb,
    private val cachedEndpointDb: CachedEndpointDb,
    private val connectivityUtils: ConnectivityUtils,
    private val postExecutionThread: PostExecutionThread
) : ProjectsRepository {

    override fun getProjects(forceRefresh: Boolean): Single<List<Project>> {
        if (forceRefresh || !cachedEndpointDb.hasCachedEndpoint("GetProjects")) {
            return refreshFromAPI()
        }
        return getFromLocalDB()
    }

    override fun getProject(id: String): Project? {
        return projectDb.getProject(id)
    }

    override fun getProjectAsFlow(id: String): Flow<Project?> {
        return projectDb.getProjectAsFlow(id)
    }

    private fun refreshFromAPI(): Single<List<Project>> {
        return endpoint.getProjects().map { rawProjects ->
            rawProjects.forEach { projectRes ->
                val offTimes = endpoint.getProjectOffTime(projectRes.id).blockingGet()
                if (offTimes.offTimes != null) {
                    projectRes.offTimes = offTimes.offTimes
                }
            }
            rawProjects
        }.observeOn(postExecutionThread.scheduler).flatMap { computedProjects ->
            computedProjects.forEach { project ->
                projectDb.insertOrUpdate(project)
            }
            cachedEndpointDb.updateCachedEndpoint("GetProjects")
            getFromLocalDB()
        }
    }

    private fun getFromLocalDB(): Single<List<Project>> {
        return Single.just(projectDb.getProjects())
    }
}
