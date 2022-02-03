package org.rfcx.incidents.data.api.project

import io.reactivex.Single
import org.rfcx.incidents.data.remote.domain.SingleUseCase
import org.rfcx.incidents.data.remote.domain.executor.PostExecutionThread
import org.rfcx.incidents.data.remote.domain.executor.ThreadExecutor
import org.rfcx.incidents.entity.project.Project

class GetProjectsUseCase(
    private val repository: ProjectsRepository,
    threadExecutor: ThreadExecutor,
    postExecutionThread: PostExecutionThread
) : SingleUseCase<GetProjectsOptions, List<Project>>(threadExecutor, postExecutionThread) {

    override fun buildUseCaseObservable(options: GetProjectsOptions): Single<List<Project>> {
        return repository.getProjects(options)
    }
}


