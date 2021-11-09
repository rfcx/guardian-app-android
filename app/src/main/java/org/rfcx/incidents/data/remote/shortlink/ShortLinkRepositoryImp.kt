package org.rfcx.incidents.data.remote.shortlink

import io.reactivex.Single
import okhttp3.ResponseBody
import org.rfcx.incidents.entity.shortlink.ShortLinkRequest

class ShortLinkRepositoryImp(private val shortLinkEndpoint: ShortLinkEndpoint) : ShortLinkRepository {
	override fun sendShortLinkRequest(sendBody: ShortLinkRequest): Single<ResponseBody> {
		return shortLinkEndpoint.sendShortLinksRequest(sendBody)
	}
	
}
