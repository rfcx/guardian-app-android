package org.rfcx.incidents.data

import io.reactivex.Single
import org.rfcx.incidents.data.interfaces.GetStreamsRepository
import org.rfcx.incidents.data.remote.streams.GetStreamsEndpoint
import org.rfcx.incidents.data.remote.streams.StreamResponse
import org.rfcx.incidents.data.remote.streams.StreamsRequestFactory

class GetStreamsRepositoryImp(private val endpoint: GetStreamsEndpoint) : GetStreamsRepository {
    override fun getStreams(requestFactory: StreamsRequestFactory): Single<List<StreamResponse>> {
        return endpoint.getStreams(
            requestFactory.limit,
            requestFactory.offset,
            requestFactory.limitIncidents,
            requestFactory.projects
        )
    }
}
