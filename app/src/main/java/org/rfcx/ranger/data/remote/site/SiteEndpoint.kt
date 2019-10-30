package org.rfcx.ranger.data.remote.site

import io.reactivex.Single
import org.rfcx.ranger.entity.site.SiteResponse
import retrofit2.http.GET
import retrofit2.http.Path

interface SiteEndpoint {
	@GET("sites/{id}")
	fun site(@Path("id") id: String): Single<List<SiteResponse>>
}