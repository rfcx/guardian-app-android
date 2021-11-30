package org.rfcx.incidents.data.api.media

import io.reactivex.Single
import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.Path

interface MediaEndpoint {
	@GET("media/{filename}")
	fun filename(@Path("filename") filename: String): Single<ResponseBody>
}
