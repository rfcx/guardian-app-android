package org.rfcx.ranger.data.remote.shortlink

import io.reactivex.Single
import org.rfcx.ranger.entity.shortlink.ShortLinkRequest

class ShortLinkRepositoryImp(private val shortLinkEndpoint: ShortLinkEndpoint) : ShortLinkRepository {
	override fun sendShortLinkRequest(sendBody: ShortLinkRequest): Single<String> {
		return shortLinkEndpoint.sendShortLinksRequest(sendBody)
	}
	
}