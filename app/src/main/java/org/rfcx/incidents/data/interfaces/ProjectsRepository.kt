package org.rfcx.incidents.data.interfaces

import io.reactivex.Single
import kotlinx.coroutines.flow.Flow
import org.rfcx.incidents.entity.stream.Project

interface ProjectsRepository {
    fun getProjects(forceRefresh: Boolean): Single<List<Project>>
    fun getProject(id: String): Project?

    fun getProjectAsFlow(id: String): Flow<Project?>
}
