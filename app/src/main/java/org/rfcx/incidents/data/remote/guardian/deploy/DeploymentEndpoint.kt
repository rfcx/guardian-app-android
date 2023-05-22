package org.rfcx.incidents.data.remote.guardian.deploy

import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import org.rfcx.incidents.entity.guardian.deployment.DeploymentRequest
import org.rfcx.incidents.entity.guardian.deployment.EditDeploymentRequest
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path

interface DeploymentEndpoint {
    @POST("deployments")
    fun createDeployment(@Body deploymentRequest: DeploymentRequest): Call<ResponseBody>

    @POST("deployments")
    suspend fun createDeploymentBySuspend(@Body deploymentRequest: DeploymentRequest): Response<ResponseBody>

    @Multipart
    @POST("deployments/{id}/assets")
    fun uploadImage(
        @Path("id") id: String,
        @Part file: MultipartBody.Part,
        @Part("meta") params: RequestBody? = null,
    ): Call<ResponseBody>

    @PATCH("deployments/{id}")
    fun editDeployment(
        @Path("id") id: String,
        @Body editDeploymentRequest: EditDeploymentRequest
    ): Call<ResponseBody>
}
