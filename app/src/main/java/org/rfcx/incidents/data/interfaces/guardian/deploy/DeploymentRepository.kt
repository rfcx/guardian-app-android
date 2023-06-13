package org.rfcx.incidents.data.interfaces.guardian.deploy

import kotlinx.coroutines.flow.Flow
import org.rfcx.incidents.entity.guardian.deployment.Deployment
import org.rfcx.incidents.data.remote.common.Result
import org.rfcx.incidents.domain.guardian.deploy.GetStreamWithDeploymentParams
import org.rfcx.incidents.entity.guardian.image.DeploymentImage
import org.rfcx.incidents.entity.stream.Stream
import org.rfcx.incidents.view.guardian.checklist.photos.Image

interface DeploymentRepository {
    fun save(stream: Stream)

    fun getAsFlow(): Flow<List<Deployment>>

    fun getByIdAsFlow(id: Int): Flow<Deployment?>

    fun get(params: GetStreamWithDeploymentParams): Flow<Result<List<Stream>>>

    fun upload(streamId: Int): Flow<Result<String>>

    fun uploadImages(deploymentId: String): Flow<Result<Boolean>>

    fun addImages(deploymentId: Int, images: List<Image>)

    fun listImages(deploymentId: Int): Flow<Result<List<DeploymentImage>>>
}
