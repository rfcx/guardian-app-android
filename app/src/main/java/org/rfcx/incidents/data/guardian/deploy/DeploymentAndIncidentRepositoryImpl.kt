package org.rfcx.incidents.data.guardian.deploy

import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import org.rfcx.incidents.data.interfaces.guardian.deploy.DeploymentAndIncidentRepository
import org.rfcx.incidents.data.local.CachedEndpointDb
import org.rfcx.incidents.data.local.EventDb
import org.rfcx.incidents.data.local.StreamDb
import org.rfcx.incidents.data.local.deploy.DeploymentDb
import org.rfcx.incidents.data.local.deploy.DeploymentImageDb
import org.rfcx.incidents.data.remote.common.Result
import org.rfcx.incidents.data.remote.guardian.deploy.DeploymentEndpoint
import org.rfcx.incidents.data.remote.streams.IncidentEndpoint
import org.rfcx.incidents.data.remote.streams.toEvent
import org.rfcx.incidents.data.remote.streams.toStream
import org.rfcx.incidents.domain.guardian.deploy.GetStreamWithDeploymentAndIncidentParams
import org.rfcx.incidents.entity.guardian.deployment.Deployment
import org.rfcx.incidents.entity.response.SyncState
import org.rfcx.incidents.entity.stream.Stream
import org.rfcx.incidents.util.ConnectivityUtils

class DeploymentAndIncidentRepositoryImpl(
    private val deploymentLocal: DeploymentDb,
    private val imageLocal: DeploymentImageDb,
    private val streamLocal: StreamDb,
    private val eventLocal: EventDb,
    private val cachedEndpointDb: CachedEndpointDb,
    private val deploymentEndpoint: DeploymentEndpoint,
    private val incidentEndpoint: IncidentEndpoint,
    private val connectivityUtils: ConnectivityUtils
) : DeploymentAndIncidentRepository {

    private var currentRunning = ""

    private fun getLocal(projectId: String): List<Stream> {
        return streamLocal.getByProject(projectId, false)
    }

    private fun getRemote(projectId: String, offset: Int): Flow<Result<List<Stream>>> {
        return flow {
            emit(Result.Loading)
            currentRunning = cacheKey(projectId)

            streamLocal.deleteByProject(projectId)
            coroutineScope {
                var order = offset
                val rawStreamsWithDeploymentDeferred = async { deploymentEndpoint.getStreams(projects = listOf(projectId), offset = offset) }
                val rawStreamsWithIncidentDeferred = async { incidentEndpoint.getStreamsSuspend(projects = listOf(projectId), offset = offset) }

                val rawStreamsWithDeployment = rawStreamsWithDeploymentDeferred.await()
                val rawStreamsWithIncident = rawStreamsWithIncidentDeferred.await()

                // save stream and deployment
                rawStreamsWithDeployment.forEachIndexed { index, rawDP ->
                    var deployment: Deployment? = null
                    rawDP.deployment?.let {
                        deployment = deploymentLocal.insertWithResult(it.toDeployment())
                    }

                    rawDP.toStream().apply {
                        this.deployment = deployment
                        order += index
                        this.order = order
                        streamLocal.insertOrUpdate(this)
                    }
                }
                // save stream and incident
                rawStreamsWithIncident.forEachIndexed { index, rawICD ->
                    val stream = rawICD.toStream()
                    order += index
                    stream.order = order
                    streamLocal.insertOrUpdate(stream)
                    rawICD.lastIncident()?.events?.forEach { event ->
                        eventLocal.insertOrUpdate(event.toEvent(rawICD.id), rawICD.lastIncident()!!.id)
                    }
                }

                cachedEndpointDb.updateCachedEndpoint(cacheKey(projectId))
                currentRunning = ""
                emit(Result.Success(getLocal(projectId)))
            }
        }.catch {
            emit(Result.Error(it))
            cachedEndpointDb.updateCachedEndpoint(cacheKey(projectId))
            currentRunning = ""
            emit(Result.Success(getLocal(projectId)))
        }
    }

    override fun get(params: GetStreamWithDeploymentAndIncidentParams): Flow<Result<List<Stream>>> {
        if (
            connectivityUtils.isAvailable() &&
            (
                params.forceRefresh || !cachedEndpointDb.hasCachedEndpoint(cacheKey(params.projectId)) &&
                    currentRunning.isEmpty()
                )
        ) {
            if (getUnsyncedWorks(params.projectId) && !params.fromAlertUnsynced) {
                return flow {
                    emit(Result.Error(UnSyncedExistException()))
                    emit(Result.Success(getLocal(params.projectId)))
                }
            }
            return getRemote(params.projectId, params.offset)
        }
        return flow { emit(Result.Success(getLocal(params.projectId))) }
    }

    private fun getUnsyncedWorks(projectId: String): Boolean {
        var hasUnsynced = false
        val deployments = streamLocal.getByProject(projectId, true).mapNotNull { it.deployment }
        deployments.forEach { dp ->
            if (dp.syncState != SyncState.SENT.value) {
                hasUnsynced = true
            }
            dp.images?.forEach { image ->
                if (image.syncState != SyncState.SENT.value) {
                    hasUnsynced = true
                }
            }
        }
        return hasUnsynced
    }

    private fun cacheKey(projectId: String): String {
        return "GetDeploymentsIncidents-$projectId"
    }
}

class UnSyncedExistException() : Throwable() {
    override val message: String
        get() = "Updating current data won't start due to unsynced deployments or images"
}
