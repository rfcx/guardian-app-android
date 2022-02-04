package org.rfcx.incidents.domain

import org.rfcx.incidents.data.interfaces.ProjectsRepository
import org.rfcx.incidents.entity.project.Project

class GetProjectUseCaseImpl(private val repository: ProjectsRepository) : GetProjectUseCase {
    override fun getProjectFromLocal(id: Int): Project? {
        return repository.getProjectFromLocal(id)
    }
}

interface GetProjectUseCase {
    fun getProjectFromLocal(id: Int): Project?
}
