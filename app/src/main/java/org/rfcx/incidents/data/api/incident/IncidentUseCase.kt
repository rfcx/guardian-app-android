package org.rfcx.incidents.data.api.incident

import io.reactivex.Single
import org.rfcx.incidents.data.remote.domain.SingleUseCase
import org.rfcx.incidents.data.remote.domain.executor.PostExecutionThread
import org.rfcx.incidents.data.remote.domain.executor.ThreadExecutor

class IncidentUseCase(private val repository: IncidentRepository,
                      threadExecutor: ThreadExecutor, postExecutionThread: PostExecutionThread
) : SingleUseCase<IncidentRequestFactory, List<IncidentsResponse>>(threadExecutor, postExecutionThread) {
	override fun buildUseCaseObservable(params: IncidentRequestFactory): Single<List<IncidentsResponse>> {
		return repository.getIncidents(params)
	}
}
