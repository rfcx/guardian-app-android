package org.rfcx.ranger.data.api.project

import io.reactivex.Single
import org.rfcx.ranger.data.remote.domain.SingleUseCase
import org.rfcx.ranger.data.remote.domain.executor.PostExecutionThread
import org.rfcx.ranger.data.remote.domain.executor.ThreadExecutor

class GetProjectsUseCase(private val repository: GetProjectsRepository,
                         threadExecutor: ThreadExecutor, postExecutionThread: PostExecutionThread
) : SingleUseCase<ProjectsRequestFactory, List<ProjectResponse>>(threadExecutor, postExecutionThread) {
	override fun buildUseCaseObservable(params: ProjectsRequestFactory): Single<List<ProjectResponse>> {
		return repository.getProjects(params)
	}
}
