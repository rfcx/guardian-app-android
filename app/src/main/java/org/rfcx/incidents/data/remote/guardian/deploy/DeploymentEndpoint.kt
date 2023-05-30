package org.rfcx.incidents.data.remote.guardian.deploy

import io.reactivex.Single
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import org.rfcx.incidents.data.remote.streams.StreamDeviceAPIResponse
import org.rfcx.incidents.entity.guardian.deployment.DeploymentRequest
import org.rfcx.incidents.entity.guardian.deployment.EditDeploymentRequest
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface DeploymentEndpoint {
    @POST("deployments")
    fun createDeployment(@Body deploymentRequest: DeploymentRequest): Call<ResponseBody>

    @GET("deployments/{id}")
    fun getDeployment(
        @Path("id") id: String
    ): Call<DeploymentResponse>

    @GET("deployments")
    fun getDeployments(
        @Query("streamIds") ids: List<String>?
    ): Single<List<DeploymentsResponse>>

    @GET("streams")
    suspend fun getStreams(
        @Query("limit") limit: Int = 100,
        @Query("offset") offset: Int = 0,
        @Query("updated_after", encoded = true) updatedAfter: String? = null,
        @Query("sort", encoded = true) sort: String? = null,
        @Query("projects") projects: List<String>? = null,
        @Query("type") type: String = "guardian"
    ): List<StreamDeviceAPIResponse>

    @POST("deployments")
    suspend fun createDeploymentBySuspend(@Body deploymentRequest: DeploymentRequest): Response<ResponseBody>

    @GET("deployments/{id}")
    suspend fun getDeploymentBySuspend(
        @Path("id") id: String
    ): DeploymentResponse

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
