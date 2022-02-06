package org.rfcx.incidents.domain

import io.reactivex.Single
import org.rfcx.incidents.data.interfaces.ProjectsRepository
import org.rfcx.incidents.domain.base.SingleUseCase
import org.rfcx.incidents.domain.executor.PostExecutionThread
import org.rfcx.incidents.domain.executor.ThreadExecutor
import org.rfcx.incidents.entity.stream.Project

class GetProjectsUseCase(
    private val repository: ProjectsRepository,
    threadExecutor: ThreadExecutor,
    postExecutionThread: PostExecutionThread
) : SingleUseCase<GetProjectsParams, List<Project>>(threadExecutor, postExecutionThread) {

    override fun buildUseCaseObservable(options: GetProjectsParams): Single<List<Project>> {
        return repository.getProjects(options.forceRefresh)
    }
}

data class GetProjectsParams(
    val forceRefresh: Boolean = false
)
