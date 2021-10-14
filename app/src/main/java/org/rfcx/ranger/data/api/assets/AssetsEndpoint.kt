package org.rfcx.ranger.data.api.assets

import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path

interface AssetsEndpoint {
	@Multipart
	@POST("responses/{id}/assets")
	fun uploadAssets(@Path("id") id: String, @Part file: MultipartBody.Part): Call<ResponseBody>
}
