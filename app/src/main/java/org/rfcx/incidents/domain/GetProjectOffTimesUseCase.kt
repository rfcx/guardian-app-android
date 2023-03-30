package org.rfcx.incidents.domain

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.rfcx.incidents.data.interfaces.ProjectsRepository
import org.rfcx.incidents.domain.base.FlowWithParamUseCase

class GetProjectOffTimesUseCase(private val repository: ProjectsRepository) : FlowWithParamUseCase<GetProjectOffTimesParams, String>() {
    override fun performAction(param: GetProjectOffTimesParams): Flow<String> {
        return repository.getProjectAsFlow(param.id).map { project ->
            project?.offTimes ?: ""
        }
    }
}

data class GetProjectOffTimesParams(
    val id: String
)
