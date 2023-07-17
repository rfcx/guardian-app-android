package org.rfcx.incidents.domain

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.rfcx.incidents.data.interfaces.ProjectsRepository
import org.rfcx.incidents.domain.base.FlowWithParamUseCase
import org.rfcx.incidents.entity.stream.Project

class GetLocalProjectUseCase(
    private val projectsRepository: ProjectsRepository
) : FlowWithParamUseCase<GetLocalProjectsParams, Project?>() {
    override fun performAction(param: GetLocalProjectsParams): Flow<Project?> {
        return flow {
            emit(projectsRepository.getProject(param.projectId))
        }
    }
}

data class GetLocalProjectsParams(
    val projectId: String
)
