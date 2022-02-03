package org.rfcx.incidents.data.api.project

import io.reactivex.Single
import org.rfcx.incidents.entity.project.Project

interface ProjectsRepository {
    fun getProjects(requestFactory: GetProjectsOptions): Single<List<Project>>
}

data class GetProjectsOptions(
    val forceRefresh: Boolean = false
)
