package org.rfcx.incidents.data.guardian.deploy

import com.google.gson.Gson
import com.google.gson.JsonObject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.rfcx.incidents.data.interfaces.guardian.deploy.DeploymentRepository
import org.rfcx.incidents.data.local.CachedEndpointDb
import org.rfcx.incidents.data.local.StreamDb
import org.rfcx.incidents.data.local.deploy.DeploymentDb
import org.rfcx.incidents.data.local.deploy.DeploymentImageDb
import org.rfcx.incidents.data.remote.common.Result
import org.rfcx.incidents.data.remote.guardian.deploy.DeploymentEndpoint
import org.rfcx.incidents.data.remote.streams.realmList
import org.rfcx.incidents.data.remote.streams.toStream
import org.rfcx.incidents.domain.guardian.deploy.GetStreamWithDeploymentParams
import org.rfcx.incidents.entity.guardian.deployment.Deployment
import org.rfcx.incidents.entity.guardian.deployment.EditDeploymentRequest
import org.rfcx.incidents.entity.guardian.deployment.toDeploymentRequestBody
import org.rfcx.incidents.entity.guardian.deployment.toRequestBody
import org.rfcx.incidents.entity.guardian.image.DeploymentImage
import org.rfcx.incidents.entity.stream.Stream
import org.rfcx.incidents.service.guardianfile.GuardianFileHelper
import org.rfcx.incidents.util.ConnectivityUtils
import org.rfcx.incidents.util.FileUtils.getMimeType
import org.rfcx.incidents.view.guardian.checklist.photos.Image
import java.io.File

class DeploymentRepositoryImpl(
    private val deploymentLocal: DeploymentDb,
    private val imageLocal: DeploymentImageDb,
    private val streamLocal: StreamDb,
    private val cachedEndpointDb: CachedEndpointDb,
    private val deploymentEndpoint: DeploymentEndpoint,
    private val guardianFileHelper: GuardianFileHelper,
    private val connectivityUtils: ConnectivityUtils
) : DeploymentRepository {

    override fun save(stream: Stream) {
        // Save image first before insert deployment
        stream.deployment?.let { deployment ->
            if (deployment.images != null) {
                val images = deployment.images!!.map {
                    imageLocal.insertWithResult(it)
                }
                deployment.images = realmList(images)
            }
            val tempDeployment = deploymentLocal.insertWithResult(deployment)
            stream.deployment = tempDeployment
            streamLocal.insertOrUpdate(stream)
        }
    }

    override fun getAsFlow(): Flow<List<Deployment>> {
        return deploymentLocal.listAsFlow()
    }

    override fun getByIdAsFlow(id: Int): Flow<Deployment?> {
        return deploymentLocal.getByIdAsFlow(id)
    }

    override fun get(params: GetStreamWithDeploymentParams): Flow<Result<List<Stream>>> {
        if ((params.forceRefresh || !cachedEndpointDb.hasCachedEndpoint(deploymentCacheKey(params.projectId ?: "null"))) && connectivityUtils.isAvailable()) {
            return refreshFromAPI(params.projectId, params.offset)
        }
        return flow { emit(Result.Success(getFromLocalDB(params.projectId))) }
    }

    private fun getFromLocalDB(projectId: String?): List<Stream> {
        return streamLocal.getByProject(projectId)
    }

    private fun refreshFromAPI(projectId: String?, offset: Int = 0): Flow<Result<List<Stream>>> {
        return flow {
            emit(Result.Loading)
            val projects = if (projectId == null) null else listOf(projectId)
            val rawStreams = deploymentEndpoint.getStreams(projects = projects, offset = offset)
            rawStreams.forEach { raw ->
                var deployment: Deployment? = null
                raw.deployment?.let {
                    deployment = deploymentLocal.insertWithResult(it.toDeployment())
                }

                raw.toStream().apply {
                    this.deployment = deployment
                    streamLocal.insertOrUpdate(this)
                }
            }
            cachedEndpointDb.updateCachedEndpoint(deploymentCacheKey(projectId ?: "null"))
            emit(Result.Success(getFromLocalDB(projectId)))
        }.catch {
            emit(Result.Error(it))
            cachedEndpointDb.updateCachedEndpoint(deploymentCacheKey(projectId ?: "null"))
            emit(Result.Success(getFromLocalDB(projectId)))
        }
    }

    override fun upload(streamId: Int): Flow<Result<String>> {
        return flow {
            val stream = streamLocal.get(streamId, false)
            stream?.deployment?.let { dp ->
                // try upload deployment
                emit(Result.Loading)
                deploymentLocal.markSending(dp.id)
                if (dp.externalId != null) {
                    val result = deploymentEndpoint.editDeploymentBySuspend(dp.externalId!!, EditDeploymentRequest(stream.toRequestBody()))
                    val error = result.errorBody()?.string()
                    if (result.isSuccessful) {
                        deploymentLocal.markSent(dp.externalId!!, dp.id)
                        emit(Result.Success(dp.externalId!!))
                    } else {
                        deploymentLocal.markUnsent(dp.id)
                        emit(Result.Error(Throwable(error)))
                    }
                } else {
                    val result = deploymentEndpoint.createDeploymentBySuspend(stream.toDeploymentRequestBody())
                    val error = result.errorBody()?.string()
                    when {
                        result.isSuccessful -> {
                            val fullId = result.headers()["Location"]
                            val idDp = fullId?.substring(fullId.lastIndexOf("/") + 1, fullId.length) ?: ""
                            deploymentLocal.markSent(idDp, dp.id)

                            val updatedDp = deploymentEndpoint.getDeploymentBySuspend(idDp)
                            streamLocal.updateSiteServerId(stream, updatedDp.stream!!.id)

                            emit(Result.Success(idDp))
                        }
                        error?.contains("this deploymentKey is already existed") ?: false -> {
                            deploymentLocal.markSent(dp.deploymentKey, dp.id)

                            val updatedDp = deploymentEndpoint.getDeploymentBySuspend(dp.deploymentKey)
                            streamLocal.updateSiteServerId(stream, updatedDp.stream!!.id)
                            emit(Result.Success(dp.deploymentKey))
                        }
                        else -> {
                            deploymentLocal.markUnsent(dp.id)
                            emit(Result.Error(Throwable(error)))
                        }
                    }
                }
            }
        }.catch {
            val stream = streamLocal.get(streamId)
            stream?.deployment?.let { dp ->
                deploymentLocal.markUnsent(dp.id)
            }
            emit(Result.Error(it))
        }
    }

    override fun uploadImages(deploymentId: String): Flow<Result<Boolean>> {
        if (!connectivityUtils.isAvailable()) {
            return flow {
                emit(Result.Error(Throwable("Cannot upload images due to unavailable connectivity")))
            }
        } else {
            val deployment = deploymentLocal.getById(deploymentId)
            return flow {
                emit(Result.Loading)
                var someFailed = false
                if (deployment != null) {
                    val images = deployment.images?.filter { it.remotePath == null }
                    images?.forEach { image ->
                        imageLocal.lockUnsent(image.id)

                        val file = File(image.localPath)
                        val mimeType = file.getMimeType()
                        val requestFile = RequestBody.create(mimeType.toMediaTypeOrNull(), guardianFileHelper.compressFile(file))
                        val body = MultipartBody.Part.createFormData("file", file.name, requestFile)
                        val gson = Gson()
                        val obj = JsonObject()
                        obj.addProperty("label", image.imageLabel)
                        val label = RequestBody.create("application/json; charset=utf-8".toMediaTypeOrNull(), gson.toJson(obj))
                        val result = deploymentEndpoint.uploadImageSuspend(deployment.deploymentKey, body, label)
                        if (result.isSuccessful) {
                            val assetPath = result.headers()["Location"]
                            assetPath?.let { path ->
                                imageLocal.markSent(image.id, path.substring(1, path.length))
                                refreshImagesInDeployment(deployment.id)
                            }
                        } else {
                            imageLocal.markUnsent(image.id)
                            someFailed = true
                        }
                    }
                    if (someFailed) {
                        emit(Result.Error(Throwable("There is something wrong on uploading images")))
                    } else {
                        emit(Result.Success(true))
                    }
                }
            }.catch {
                deployment?.images?.forEach { image ->
                    imageLocal.markUnsent(image.id)
                }
                emit(Result.Error(Throwable("There is something wrong on uploading images")))
            }
        }
    }

    private fun refreshImagesInDeployment(deploymentId: Int) {
        val deployment = deploymentLocal.getById(deploymentId)
        deployment?.images?.let { im ->
            val ids = im.map { it.id }
            val images = imageLocal.getByIds(ids)
            deployment.images = realmList(images)
            deploymentLocal.insert(deployment)
        }
    }

    override fun addImages(deploymentId: Int, images: List<Image>) {
        val deployment = deploymentLocal.getById(deploymentId)
        deployment?.let { dp ->
            val newImages = images.map {
                DeploymentImage(
                    localPath = it.path!!, imageLabel = it.name
                )
            }.map {
                imageLocal.insertWithResult(it)
            }
            val tempImages = (dp.images?.toList() ?: listOf())
            dp.images = realmList(tempImages + newImages)
            deploymentLocal.insert(dp)
        }
    }

    override fun listImages(deploymentId: Int): Flow<Result<List<DeploymentImage>>> {
        return flow {
            emit(Result.Loading)

            val deployment = deploymentLocal.getById(deploymentId)
            if (deployment?.externalId != null) {
                val result = deploymentEndpoint.listImages(deployment.externalId!!)
                val newImages = result.map { imageLocal.insertWithResult(it.toDeploymentImage()) }
                val tempImages = (deployment.images?.toList() ?: listOf())
                deployment.images = realmList(tempImages.filter { it.remotePath == null } + newImages)
                deploymentLocal.insert(deployment)

                emit(Result.Success(getLocalImages(deploymentId)))
            } else {
                emit(Result.Success(emptyList()))
            }
        }.catch {
            // in case of any error
            // eg. runtime, no internet
            if (!connectivityUtils.isAvailable()) {
                emit(Result.Error(Throwable("Cannot get images due to unavailable connectivity")))
            } else {
                emit(Result.Error(it))
            }
            emit(Result.Success(getLocalImages(deploymentId)))
        }
    }

    private fun getLocalImages(deploymentId: Int): List<DeploymentImage> {
        val deployment = deploymentLocal.getById(deploymentId)
        return deployment?.images?.toList() ?: emptyList()
    }

    private fun deploymentCacheKey(projectId: String): String {
        return "GetDeployments-$projectId"
    }
}
