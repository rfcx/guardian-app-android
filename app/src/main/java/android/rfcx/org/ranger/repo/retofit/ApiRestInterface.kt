package android.rfcx.org.ranger.repo.retofit

import android.rfcx.org.ranger.entity.CheckInResult
import android.rfcx.org.ranger.entity.EventResponse
import android.rfcx.org.ranger.entity.LoginResponse
import android.rfcx.org.ranger.entity.message.Message
import retrofit2.Call
import retrofit2.http.*

/**
 * Created by Jingjoeh on 10/2/2017 AD.
 */
interface ApiRestInterface {

    @FormUrlEncoded
    @POST("users/login")
    fun login(@Field("email") email: String,
              @Field("password") pass: String,
              @Field("extended_expiration") loginRemember: Int): Call<List<LoginResponse>>

    @GET("messages")
    fun getMessage(@Header("x-auth-user") authUser: String,
                   @Header("x-auth-token") authToken: String,
                   @Query("to") userGuID: String,
                   @Query("type") type: String): Call<List<Message>>

    @FormUrlEncoded
    @POST("users/checkin")
    fun updateLocation(@Header("x-auth-user") authUser: String,
                       @Header("x-auth-token") authToken: String,
                       @Field("latitude") latitude: Double,
                       @Field("longitude") longitude: Double,
                       @Field("time") time: String): Call<CheckInResult>

    @GET("events/event")
    fun getEvents(@Header("x-auth-user") authUser: String,
                  @Header("x-auth-token") authToken: String,
                  @Query("limit") limit: Int): Call<EventResponse>
}