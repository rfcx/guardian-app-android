package org.rfcx.ranger.data.remote.site

import io.reactivex.Single
import org.rfcx.ranger.entity.site.SiteResponse

interface SiteRepository {
	fun site(id: String): Single<List<SiteResponse>>
}