package org.rfcx.incidents.data.remote.guardian.software

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Streaming
import retrofit2.http.Url

interface SoftwareEndpoint {
    @GET("v2/guardians/software/all")
    suspend fun getSoftware(): List<SoftwareResponse>
    @Streaming
    @GET
    suspend fun downloadFile(@Url url: String): ResponseBody
}
