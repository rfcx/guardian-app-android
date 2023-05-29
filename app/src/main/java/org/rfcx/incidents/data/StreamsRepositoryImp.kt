package org.rfcx.incidents.data

import android.util.Log
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.flow.Flow
import org.rfcx.incidents.data.interfaces.StreamsRepository
import org.rfcx.incidents.data.local.CachedEndpointDb
import org.rfcx.incidents.data.local.EventDb
import org.rfcx.incidents.data.local.StreamDb
import org.rfcx.incidents.data.local.deploy.DeploymentDb
import org.rfcx.incidents.data.remote.guardian.deploy.DeploymentEndpoint
import org.rfcx.incidents.data.remote.streams.IncidentEndpoint
import org.rfcx.incidents.data.remote.streams.StreamEndpoint
import org.rfcx.incidents.data.remote.streams.toEvent
import org.rfcx.incidents.data.remote.streams.toStream
import org.rfcx.incidents.domain.GetLocalStreamsParams
import org.rfcx.incidents.domain.GetStreamsParams
import org.rfcx.incidents.domain.executor.PostExecutionThread
import org.rfcx.incidents.domain.executor.ThreadExecutor
import org.rfcx.incidents.entity.guardian.deployment.Deployment
import org.rfcx.incidents.entity.stream.Stream

class StreamsRepositoryImp(
    private val incidentEndpoint: IncidentEndpoint,
    private val streamEndpoint: StreamEndpoint,
    private val streamDb: StreamDb,
    private val deploymentDb: DeploymentDb,
    private val eventDb: EventDb,
    private val cachedEndpointDb: CachedEndpointDb,
    private val postExecutionThread: PostExecutionThread,
    private val threadExecutor: ThreadExecutor
) : StreamsRepository {
    override fun get(params: GetStreamsParams): Single<List<Stream>> {
        if (params.streamRefresh) {
            var data: Single<List<Stream>>? = null
            streamDb.deleteByProject(params.projectId) { if (it) data = refreshFromAPI(params.projectId, params.offset) }
            return data ?: refreshFromAPI(params.projectId, params.offset)
        }
        if (params.forceRefresh || !cachedEndpointDb.hasCachedEndpoint(cacheKey(params.projectId))) {
            return refreshFromAPI(params.projectId, params.offset)
        }
        return getFromLocalDB(params.projectId)
    }

    override fun getLocalAsFlow(params: GetLocalStreamsParams): Flow<List<Stream>> {
        return streamDb.getAllAsFlow()
    }

    override fun getLocal(params: GetLocalStreamsParams): List<Stream> {
        return streamDb.getByProject(params.projectId)
    }

    private fun refreshFromAPI(projectId: String, offset: Int): Single<List<Stream>> {
        // Save all streams in project
        return streamEndpoint.getStreams(
            projects = listOf(projectId),
            offset = offset
        ).observeOn(postExecutionThread.scheduler)
            .flatMap { rawStreams ->
                rawStreams.forEachIndexed { index, streamRes ->
                    var deployment: Deployment? = null
                    streamRes.deployment?.let {
                        deployment = deploymentDb.insertWithResult(it.toDeployment())
                    }

                    streamRes.toStream().apply {
                        this.deployment = deployment
                        streamDb.insertOrUpdate(this)
                    }
                }

                // Load all possible stream with incident in project
                incidentEndpoint.getStreams(projects = listOf(projectId),
                    offset = offset
                )
                    .subscribeOn(Schedulers.from(threadExecutor))
                    .observeOn(postExecutionThread.scheduler)
                    .flatMap { rawIncidents ->
                        rawIncidents.forEachIndexed { index, streamRes ->
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
            }.onErrorResumeNext {
                Log.d("GuardianApp", "$it")
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
