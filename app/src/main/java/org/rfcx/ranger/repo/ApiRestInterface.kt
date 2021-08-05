package org.rfcx.ranger.repo

import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.rfcx.ranger.entity.event.EventsResponse
import org.rfcx.ranger.entity.event.ReviewEventResponse
import org.rfcx.ranger.entity.guardian.Site
import org.rfcx.ranger.entity.location.CheckInRequest
import org.rfcx.ranger.entity.location.CheckInResult
import org.rfcx.ranger.entity.message.Message
import org.rfcx.ranger.entity.report.SendReportResponse
import org.rfcx.ranger.entity.report.UploadImageResponse
import retrofit2.Call
import retrofit2.http.*

interface ApiRestInterface {
	
	@GET("v1/messages")
	fun getMessage(@Header("Authorization") authUser: String,
	               @Query("to") email: String,
	               @Query("type") type: String): Call<List<Message>>
	
	@POST("v1/users/checkin")
	fun updateLocation(@Header("Authorization") authUser: String,
	                   @Body checkInRequestBody: CheckInRequest): Call<List<CheckInResult>>
	
	@POST("v1/reports")
	@Multipart
	fun sendReport(@Header("Authorization") authUser: String,
	               @Part("value") value: RequestBody,
	               @Part("site") site: RequestBody, @Part("reported_at") reportedAt: RequestBody,
	               @Part("lat") latitude: RequestBody, @Part("long") longitude: RequestBody,
	               @Part("age_estimate") ageEstimate: RequestBody,
	               @Part("distance") distanceEstimate: RequestBody? = null,
	               @Part("notes") notes: RequestBody?,
	               @Part audioFile: MultipartBody.Part? = null): Call<SendReportResponse>
	
	@POST("v1/reports/{guid}")
	@Multipart
	fun updateReport(@Header("Authorization") authUser: String,
	               @Path("guid") guid: String,
	               @Part("value") value: RequestBody,
	               @Part("site") site: RequestBody, @Part("reported_at") reportedAt: RequestBody,
	               @Part("lat") latitude: RequestBody, @Part("long") longitude: RequestBody,
	               @Part("age_estimate") ageEstimate: RequestBody,
	               @Part("distance") distanceEstimate: RequestBody? = null,
	               @Part("notes") notes: RequestBody?,
	               @Part audioFile: MultipartBody.Part? = null): Call<SendReportResponse>
	
	@GET("v1/sites/{id}")
	fun site(@Header("Authorization") authorization: String, @Path("id") id: String): Call<Site>
	
	@POST("v1/reports/{report_guid}/attachments")
	@Multipart
	fun uploadImages(@Header("Authorization") authUser: String,
	                 @Path("report_guid") reportGuID: String,
	                 @Part("type") type: RequestBody,
	                 @Part("time") time: RequestBody,
	                 @Part() audioFile: ArrayList<MultipartBody.Part>): Call<List<UploadImageResponse>>
	
}
