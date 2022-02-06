package org.rfcx.incidents.data.interfaces

import io.reactivex.Single
import org.rfcx.incidents.entity.stream.Project

interface ProjectsRepository {
    fun getProjects(forceRefresh: Boolean): Single<List<Project>>
    fun getProject(id: String): Project?
}
