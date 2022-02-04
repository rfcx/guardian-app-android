package org.rfcx.incidents.data.interfaces

import io.reactivex.Single
import org.rfcx.incidents.entity.project.Project

interface ProjectsRepository {
    fun getProjects(forceRefresh: Boolean): Single<List<Project>>
}
