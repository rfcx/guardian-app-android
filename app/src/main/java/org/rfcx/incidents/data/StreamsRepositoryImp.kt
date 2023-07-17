package org.rfcx.incidents.data

import io.reactivex.Single
import kotlinx.coroutines.flow.Flow
import org.rfcx.incidents.data.interfaces.StreamsRepository
import org.rfcx.incidents.data.local.CachedEndpointDb
import org.rfcx.incidents.data.local.EventDb
import org.rfcx.incidents.data.local.StreamDb
import org.rfcx.incidents.data.remote.streams.IncidentEndpoint
import org.rfcx.incidents.data.remote.streams.toEvent
import org.rfcx.incidents.data.remote.streams.toStream
import org.rfcx.incidents.domain.GetLocalStreamsParams
import org.rfcx.incidents.domain.GetStreamsParams
import org.rfcx.incidents.domain.executor.PostExecutionThread
import org.rfcx.incidents.entity.stream.Stream
import org.rfcx.incidents.util.ConnectivityUtils

class StreamsRepositoryImp(
    private val incidentEndpoint: IncidentEndpoint,
    private val streamDb: StreamDb,
    private val eventDb: EventDb,
    private val cachedEndpointDb: CachedEndpointDb,
    private val connectivityUtils: ConnectivityUtils,
    private val postExecutionThread: PostExecutionThread
) : StreamsRepository {
    override fun list(params: GetStreamsParams): Single<List<Stream>> {
        if ((params.forceRefresh || !cachedEndpointDb.hasCachedEndpoint(cacheKey(params.projectId)) && connectivityUtils.isAvailable())) {
            return refreshFromAPI(params.projectId, params.offset)
        }
        return getFromLocalDB(params.projectId)
    }

    override fun listLocalAsFlow(params: GetLocalStreamsParams): Flow<List<Stream>> {
        return streamDb.getAllAsFlowByProject(params.projectId)
    }

    override fun listLocalCopyAsFlow(params: GetLocalStreamsParams): Flow<List<Stream>> {
        return streamDb.getAllCopyAsFlowByProject(params.projectId)
    }

    override fun listLocal(params: GetLocalStreamsParams): List<Stream> {
        return streamDb.getByProject(params.projectId)
    }

    override fun getById(id: Int): Stream? {
        return streamDb.get(id)
    }

    override fun getByIdAsFlow(id: Int): Flow<Stream?> {
        return streamDb.getByIdAsFlow(id)
    }

    private fun refreshFromAPI(projectId: String, offset: Int): Single<List<Stream>> {
        // Save all streams in project
        return incidentEndpoint.getStreams(
            projects = listOf(projectId),
            offset = offset
        )
            .observeOn(postExecutionThread.scheduler)
            .flatMap { rawIncidents ->
                rawIncidents.forEachIndexed { index, streamRes ->
                    val stream = streamRes.toStream()
                    stream.order = offset + index
                    streamDb.insertOrUpdate(stream)
                    streamRes.lastIncident()?.events?.forEach { event ->
                        eventDb.insertOrUpdate(event.toEvent(streamRes.id), streamRes.lastIncident()!!.id)
                    }
                }
                cachedEndpointDb.updateCachedEndpoint(cacheKey(projectId))
                getFromLocalDB(projectId)
            }
            .onErrorResumeNext {
                cachedEndpointDb.updateCachedEndpoint(cacheKey(projectId))
                getFromLocalDB(projectId)
            }
    }

    private fun getFromLocalDB(projectId: String): Single<List<Stream>> {
        return Single.just(streamDb.getByProject(projectId, false))
    }

    private fun cacheKey(projectId: String): String {
        return "GetStreams-$projectId"
    }
}
