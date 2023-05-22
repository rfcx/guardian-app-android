package org.rfcx.incidents.data.guardian.deploy

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.rfcx.incidents.data.interfaces.guardian.deploy.DeploymentRepository
import org.rfcx.incidents.data.local.StreamDb
import org.rfcx.incidents.data.local.deploy.DeploymentDb
import org.rfcx.incidents.data.local.deploy.DeploymentImageDb
import org.rfcx.incidents.data.remote.guardian.deploy.DeploymentEndpoint
import org.rfcx.incidents.data.remote.streams.realmList
import org.rfcx.incidents.entity.guardian.deployment.Deployment
import org.rfcx.incidents.entity.guardian.deployment.toRequestBody
import org.rfcx.incidents.data.remote.common.Result

class DeploymentRepositoryImpl(
    private val deploymentLocal: DeploymentDb,
    private val imageLocal: DeploymentImageDb,
    private val streamLocal: StreamDb,
    private val deploymentEndpoint: DeploymentEndpoint
) : DeploymentRepository {

    override fun save(deployment: Deployment) {
        // Save image first before insert deployment
        if (deployment.images != null) {
            val images = deployment.images!!.map {
                imageLocal.insertWithResult(it)
            }
            deployment.images = realmList(images)
        }
        if (deployment.stream != null) {
            val stream = deployment.stream!!.let {
                streamLocal.insertWithResult(it)
            }
            deployment.stream = stream
        }
        deploymentLocal.insert(deployment)
    }

    override fun get(): Flow<List<Deployment>> {
        return deploymentLocal.getAsFlow()
    }

    override fun upload(id: Int): Flow<Result<Boolean>> {
        return flow {
            val deployment = deploymentLocal.getById(id)
            deployment?.let { dp ->
                // try upload deployment
                emit(Result.Loading)
                deploymentLocal.markSending(id)
                val result = deploymentEndpoint.createDeploymentBySuspend(dp.toRequestBody())
                val error = result.errorBody()?.string()
                when {
                    result.isSuccessful -> {
                        val fullId = result.headers()["Location"]
                        val idDp = fullId?.substring(fullId.lastIndexOf("/") + 1, fullId.length) ?: ""
                        deploymentLocal.markSent(idDp, dp.id)
                        emit(Result.Success(true))
                    }
                    error?.contains("this deploymentKey is already existed") ?: false -> {
                        deploymentLocal.markSent(dp.deploymentKey, dp.id)
                        emit(Result.Success(true))
                    }
                    else -> {
                        deploymentLocal.markUnsent(dp.id)
                        emit(Result.Error(Throwable(error)))
                    }
                }
                // check for images

            }
        }
    }
}
