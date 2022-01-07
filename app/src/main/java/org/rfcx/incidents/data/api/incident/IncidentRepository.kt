package org.rfcx.incidents.data.api.incident

import io.reactivex.Single

interface IncidentRepository {
	fun getIncidents(requestFactory: IncidentRequestFactory): Single<List<IncidentsResponse>>
}
