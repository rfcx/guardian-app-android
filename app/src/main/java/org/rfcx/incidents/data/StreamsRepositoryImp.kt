package org.rfcx.incidents.data

import io.reactivex.Single
import org.rfcx.incidents.data.interfaces.StreamsRepository
import org.rfcx.incidents.data.local.CachedEndpointDb
import org.rfcx.incidents.data.local.EventDb
import org.rfcx.incidents.data.local.StreamDb
import org.rfcx.incidents.data.remote.streams.Endpoint
import org.rfcx.incidents.data.remote.streams.toEvent
import org.rfcx.incidents.data.remote.streams.toStream
import org.rfcx.incidents.domain.GetStreamsParams
import org.rfcx.incidents.domain.executor.PostExecutionThread
import org.rfcx.incidents.entity.stream.Stream

class StreamsRepositoryImp(
    private val endpoint: Endpoint,
    private val streamDb: StreamDb,
    private val eventDb: EventDb,
    private val cachedEndpointDb: CachedEndpointDb,
    private val postExecutionThread: PostExecutionThread
) : StreamsRepository {
    override fun get(params: GetStreamsParams): Single<List<Stream>> {
        if (params.forceRefresh || !cachedEndpointDb.hasCachedEndpoint(cacheKey(params.projectId))) {
            return refreshFromAPI(params.projectId, params.offset)
        }
        return getFromLocalDB(params.projectId)
    }

    private fun refreshFromAPI(projectId: String, offset: Int): Single<List<Stream>> {
        return endpoint.getStreams(projects = listOf(projectId), offset = offset).observeOn(postExecutionThread.scheduler).flatMap { rawStreams ->
            if (offset == 0) streamDb.deleteStreamsByProjectId(projectId)
            rawStreams.forEachIndexed { index, streamRes ->
                val stream = streamRes.toStream()
                stream.order = offset + index
                streamDb.insertOrUpdate(stream)
                eventDb.deleteEventsByStreamId(streamRes.id)
                streamRes.lastIncident()?.events?.forEach { event ->
                    eventDb.insertOrUpdate(event.toEvent(streamRes.id), streamRes.lastIncident()!!.id)
                }
            }
            cachedEndpointDb.updateCachedEndpoint(cacheKey(projectId))
            getFromLocalDB(projectId)
        }
    }

    private fun getFromLocalDB(projectId: String): Single<List<Stream>> {
        return Single.just(streamDb.getByProject(projectId))
    }

    private fun cacheKey(projectId: String): String {
        return "GetStreams-$projectId"
    }
}
