package org.rfcx.incidents.data.api.streams

import io.reactivex.Single

interface GetStreamsRepository {
	fun getStreams(requestFactory: StreamsRequestFactory): Single<List<StreamResponse>>
}
