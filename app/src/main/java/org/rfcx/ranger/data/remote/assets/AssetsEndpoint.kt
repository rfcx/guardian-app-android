package org.rfcx.ranger.data.remote.assets

import io.reactivex.Single
import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.Path

interface AssetsEndpoint {
	@GET("internal/assets/streams/{filename}")
	fun filename(@Path("filename") filename: String): Single<ResponseBody>
}
