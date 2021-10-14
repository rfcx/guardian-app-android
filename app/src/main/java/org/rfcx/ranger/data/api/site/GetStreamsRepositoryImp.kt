package org.rfcx.ranger.data.api.site

import io.reactivex.Single

class GetStreamsRepositoryImp(private val endpoint: GetStreamsEndpoint) : GetStreamsRepository {
	override fun getStreams(requestFactory: StreamsRequestFactory): Single<List<StreamResponse>> {
		return endpoint.getStreams(requestFactory.limit, requestFactory.offset, requestFactory.withEventsCount, requestFactory.updatedAfter, requestFactory.sort, requestFactory.projects)
	}
}
