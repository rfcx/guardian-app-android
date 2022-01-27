package org.rfcx.incidents.data.api.streams

import io.reactivex.Single

class GetStreamsRepositoryImp(private val endpoint: GetStreamsEndpoint) : GetStreamsRepository {
	override fun getStreams(requestFactory: StreamsRequestFactory): Single<List<StreamResponse>> {
		return endpoint.getStreams(requestFactory.limit, requestFactory.offset, requestFactory.limitIncidents, requestFactory.projects)
	}
}
