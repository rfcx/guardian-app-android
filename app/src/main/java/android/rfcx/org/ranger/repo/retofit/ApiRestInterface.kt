package android.rfcx.org.ranger.repo.retofit

import android.rfcx.org.ranger.entity.LoginResponse
import android.rfcx.org.ranger.entity.Message
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
                   @Query("type") type: String) : Call<List<Message>>
}