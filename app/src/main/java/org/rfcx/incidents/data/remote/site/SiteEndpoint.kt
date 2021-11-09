package org.rfcx.incidents.data.remote.site

import io.reactivex.Single
import org.rfcx.incidents.entity.site.SiteResponse
import retrofit2.http.GET
import retrofit2.http.Path

interface SiteEndpoint {
	@GET("v1/sites/{id}")
	fun site(@Path("id") id: String): Single<List<SiteResponse>>
}
