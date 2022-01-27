package org.rfcx.incidents.repo

import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.rfcx.incidents.entity.report.SendReportResponse
import retrofit2.Call
import retrofit2.http.*

interface ApiRestInterface {
    
    @POST("v1/reports")
    @Multipart
    fun sendReport(
        @Header("Authorization") authUser: String,
        @Part("value") value: RequestBody,
        @Part("site") site: RequestBody, @Part("reported_at") reportedAt: RequestBody,
        @Part("lat") latitude: RequestBody, @Part("long") longitude: RequestBody,
        @Part("age_estimate") ageEstimate: RequestBody,
        @Part("distance") distanceEstimate: RequestBody? = null,
        @Part("notes") notes: RequestBody?,
        @Part audioFile: MultipartBody.Part? = null
    ): Call<SendReportResponse>
    
    @POST("v1/reports/{guid}")
    @Multipart
    fun updateReport(
        @Header("Authorization") authUser: String,
        @Path("guid") guid: String,
        @Part("value") value: RequestBody,
        @Part("site") site: RequestBody, @Part("reported_at") reportedAt: RequestBody,
        @Part("lat") latitude: RequestBody, @Part("long") longitude: RequestBody,
        @Part("age_estimate") ageEstimate: RequestBody,
        @Part("distance") distanceEstimate: RequestBody? = null,
        @Part("notes") notes: RequestBody?,
        @Part audioFile: MultipartBody.Part? = null
    ): Call<SendReportResponse>
    
}
