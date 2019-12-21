package org.rfcx.ranger.data.remote.shortlink

import io.reactivex.Single
import okhttp3.ResponseBody
import org.rfcx.ranger.entity.shortlink.ShortLinkRequest
import retrofit2.http.Body
import retrofit2.http.POST

interface ShortLinkEndpoint {
	@POST("v1/shortlinks")
	fun sendShortLinksRequest(@Body body: ShortLinkRequest): Single<ResponseBody>
}