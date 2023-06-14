package org.rfcx.incidents.data.guardian.deploy

import android.util.Log
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import org.rfcx.incidents.data.interfaces.guardian.deploy.DeploymentAndIncidentRepository
import org.rfcx.incidents.data.interfaces.guardian.deploy.DeploymentRepository
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
import org.rfcx.incidents.entity.stream.Stream

class DeploymentAndIncidentRepositoryImpl(
    private val deploymentLocal: DeploymentDb,
    private val imageLocal: DeploymentImageDb,
    private val streamLocal: StreamDb,
    private val eventLocal: EventDb,
    private val cachedEndpointDb: CachedEndpointDb,
    private val deploymentEndpoint: DeploymentEndpoint,
    private val incidentEndpoint: IncidentEndpoint
) : DeploymentAndIncidentRepository {

    private var currentRunning = ""

    private fun getLocal(projectId: String): List<Stream> {
        return streamLocal.getByProject(projectId)
    }

    private fun getRemote(projectId: String, offset: Int): Flow<Result<List<Stream>>> {
        return flow {
            emit(Result.Loading)
            currentRunning = cacheKey(projectId)

            streamLocal.deleteByProject(projectId)
            val rawStreamsWithDeployment = deploymentEndpoint.getStreams(projects = listOf(projectId), offset = offset)
            val rawStreamsWithIncident = incidentEndpoint.getStreamsSuspend(projects = listOf(projectId), offset = offset)
            var order = offset
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
        }.catch {
            emit(Result.Error(it))
            cachedEndpointDb.updateCachedEndpoint(cacheKey(projectId))
            currentRunning = ""
            emit(Result.Success(getLocal(projectId)))
        }
    }

    override fun get(params: GetStreamWithDeploymentAndIncidentParams): Flow<Result<List<Stream>>> {
        Log.d("GuardianImageApp", "run $currentRunning")
        if (params.forceRefresh || !cachedEndpointDb.hasCachedEndpoint(cacheKey(params.projectId)) && currentRunning.isEmpty()) {
            Log.d("GuardianImageApp", "run endpoint")
            return getRemote(params.projectId, params.offset)
        }
        return flow { emit(Result.Success(getLocal(params.projectId))) }
    }

    private fun cacheKey(projectId: String): String {
        return "GetDeploymentsIncidents-$projectId"
    }
}
