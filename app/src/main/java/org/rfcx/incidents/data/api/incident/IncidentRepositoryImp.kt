package org.rfcx.incidents.data.api.incident

import io.reactivex.Single

class IncidentRepositoryImp(private val endpoint: IncidentEndpoint) : IncidentRepository {
	override fun getIncidents(requestFactory: IncidentRequestFactory): Single<List<IncidentsResponse>> {
		return endpoint.getIncident(requestFactory.limit_incidents, requestFactory.keyword)
	}
}
