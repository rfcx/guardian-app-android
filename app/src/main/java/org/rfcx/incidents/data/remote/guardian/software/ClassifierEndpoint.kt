package org.rfcx.incidents.data.remote.guardian.software

import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Streaming
import retrofit2.http.Url

interface ClassifierEndpoint {
    @GET("classifiers")
    suspend fun getClassifier(): List<ClassifierResponse>
    @Streaming
    @GET
    suspend fun downloadFile(@Url url: String): ResponseBody
}
