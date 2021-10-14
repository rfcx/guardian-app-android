package org.rfcx.ranger.data.api.site

import io.reactivex.Single

interface GetStreamsRepository {
	fun getStreams(requestFactory: StreamsRequestFactory): Single<List<StreamResponse>>
}
