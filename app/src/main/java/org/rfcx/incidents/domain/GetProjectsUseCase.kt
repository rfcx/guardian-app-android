package org.rfcx.incidents.domain

import io.reactivex.Single
import org.rfcx.incidents.data.GetProjectsOptions
import org.rfcx.incidents.data.ProjectsRepository
import org.rfcx.incidents.domain.executor.PostExecutionThread
import org.rfcx.incidents.domain.executor.ThreadExecutor
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


