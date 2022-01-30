package org.rfcx.incidents.data.remote.site

import io.reactivex.Single
import org.rfcx.incidents.entity.site.SiteResponse

class SiteRepositoryImp(private val siteEndpoint: SiteEndpoint) : SiteRepository {
    override fun site(id: String): Single<List<SiteResponse>> {
        return siteEndpoint.site(id)
    }
}
