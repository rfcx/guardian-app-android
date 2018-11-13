package org.rfcx.ranger.repo.retofit

import org.rfcx.ranger.entity.*
import org.rfcx.ranger.entity.guardian.GuardianGroup
import org.rfcx.ranger.entity.message.Message
import org.rfcx.ranger.entity.report.Report
import org.rfcx.ranger.entity.report.SendReportResponse
import retrofit2.Call
import retrofit2.http.*

interface ApiRestInterface {

    @FormUrlEncoded
    @POST("users/login")
    fun login(@Field("email") email: String,
              @Field("password") pass: String,
              @Field("extended_expiration") loginRemember: Int): Call<List<LoginResponse>>
	
	
	@GET("messages")
	fun getMessage(@Header("Authorization") authUser: String,
	               @Query("to") email: String,
	               @Query("type") type: String): Call<List<Message>>

    @FormUrlEncoded
    @POST("users/checkin")
    fun updateLocation(@Header("Authorization") authUser: String,
                       @Field("latitude") latitude: Double,
                       @Field("longitude") longitude: Double,
                       @Field("time") time: String): Call<CheckInResult>

    @GET("events/event")
    fun getEvents(@Header("Authorization") authUser: String,
                  @Query("sites[]") siteId : String?,
                  @Query("order") orderBy : String,
                  @Query("dir") dir : String,
                  @Query("limit") limit: Int): Call<EventResponse>

    @POST("reports")
    fun sendReport(@Header("Authorization") authUser: String,
                   @Body report: Report): Call<SendReportResponse>

    @POST("events/{event_guid}/{review_confirmed}")
    fun reviewEvent(@Header("Authorization") authUser: String,
                    @Path("event_guid") eventGuID: String,
                    @Path("review_confirmed") reviewConfirm: String)
            : Call<ReviewEventResponse>

    @GET("users/touchapi")
    fun userTouch(@Header("Authorization") authorization: String): Call<UserTouchResponse>

    @GET("guardians/groups")
    fun guardianGroups(@Header("Authorization") authorization: String): Call<List<GuardianGroup>>

}