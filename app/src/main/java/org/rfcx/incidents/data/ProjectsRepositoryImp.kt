package org.rfcx.incidents.data

import android.os.Looper
import android.util.Log
import io.reactivex.Single
import org.rfcx.incidents.data.interfaces.GetProjectsOptions
import org.rfcx.incidents.data.interfaces.ProjectsRepository
import org.rfcx.incidents.data.remote.project.ProjectsEndpoint
import org.rfcx.incidents.data.local.CachedEndpointDb
import org.rfcx.incidents.data.local.ProjectDb
import org.rfcx.incidents.domain.executor.PostExecutionThread
import org.rfcx.incidents.entity.project.Project

class ProjectsRepositoryImp(
    private val endpoint: ProjectsEndpoint,
    private val projectDb: ProjectDb,
    private val cachedEndpointDb: CachedEndpointDb,
    private val postExecutionThread: PostExecutionThread
) : ProjectsRepository {

    override fun getProjects(options: GetProjectsOptions): Single<List<Project>> {
        if (options.forceRefresh || !cachedEndpointDb.hasCachedEndpoint("GetProjects")) {
            Log.d("ProjectsRepo", "API")
            return refreshFromAPI()
        }
        Log.d("ProjectsRepo", "DB")
        return getFromLocalDB()
    }

    private fun refreshFromAPI(): Single<List<Project>> {
        Log.d("ProjectsRepo", "OUTSIDE: " + if (Looper.myLooper() == Looper.getMainLooper()) "MAIN THREAD" else "NOT MAIN!")
        return endpoint.getProjects().observeOn(postExecutionThread.scheduler).flatMap { rawProjects ->
            Log.d("ProjectsRepo", "INSIDE: " + if (Looper.myLooper() == Looper.getMainLooper()) "MAIN THREAD" else "NOT MAIN!")
            rawProjects.forEach {
                projectDb.insertOrUpdate(it)
            }
            cachedEndpointDb.updateCachedEndpoint("GetProjects")
            getFromLocalDB()
        }
    }

    private fun getFromLocalDB(): Single<List<Project>> {
        return Single.just(projectDb.getProjects())
    }
}
