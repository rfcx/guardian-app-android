package org.rfcx.incidents.data.remote.site

import io.reactivex.Single
import org.rfcx.incidents.entity.site.SiteResponse

interface SiteRepository {
	fun site(id: String): Single<List<SiteResponse>>
}
