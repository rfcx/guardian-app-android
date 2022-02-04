package org.rfcx.incidents.data.interfaces

import io.reactivex.Single
import org.rfcx.incidents.data.remote.streams.StreamResponse
import org.rfcx.incidents.data.remote.streams.StreamsRequestFactory

interface GetStreamsRepository {
    fun getStreams(requestFactory: StreamsRequestFactory): Single<List<StreamResponse>>
}
