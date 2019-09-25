package org.rfcx.ranger.repo

import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.rfcx.ranger.entity.event.ClassificationBody
import org.rfcx.ranger.entity.event.ClassificationResponse
import org.rfcx.ranger.entity.event.EventResponse
import org.rfcx.ranger.entity.event.ReviewEventResponse
import org.rfcx.ranger.entity.guardian.GuardianGroup
import org.rfcx.ranger.entity.guardian.Site
import org.rfcx.ranger.entity.location.CheckInRequest
import org.rfcx.ranger.entity.location.CheckInResult
import org.rfcx.ranger.entity.message.Message
import org.rfcx.ranger.entity.report.SendReportResponse
import org.rfcx.ranger.entity.report.UploadImageResponse
import org.rfcx.ranger.entity.user.InvitationCodeRequest
import org.rfcx.ranger.entity.user.InvitationCodeResponse
import org.rfcx.ranger.entity.user.UserTouchResponse
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
	
	@GET("v2/events/event/datatable")
	fun getEvents(@Header("Authorization") authUser: String,
	              @Query("guardian_groups[]") guardianGroup: String,
	              @Query("order") orderBy: String,
	              @Query("dir") dir: String,
	              @Query("limit") limit: Int,
	              @Query("offset") offset: Int): Call<EventResponse>
	
	@POST("v1/reports")
	@Multipart
	fun sendReport(@Header("Authorization") authUser: String, @Part("value") value: RequestBody,
	               @Part("site") site: RequestBody, @Part("reported_at") reportedAt: RequestBody,
	               @Part("lat") latitude: RequestBody, @Part("long") longitude: RequestBody,
	               @Part("age_estimate") ageEstimate: RequestBody, @Part("distance") distanceEstimate: RequestBody? = null,
	               @Part() audioFile: MultipartBody.Part? = null): Call<SendReportResponse>
	
	@POST("v1/events/{event_guid}/{review_confirmed}")
	fun reviewEvent(@Header("Authorization") authUser: String,
	                @Path("event_guid") eventGuID: String,
	                @Path("review_confirmed") reviewConfirm: String)
			: Call<ReviewEventResponse>
	
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