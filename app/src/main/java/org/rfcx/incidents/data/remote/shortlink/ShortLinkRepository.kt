package org.rfcx.incidents.data.remote.shortlink

import io.reactivex.Single
import okhttp3.ResponseBody
import org.rfcx.incidents.entity.shortlink.ShortLinkRequest

interface ShortLinkRepository {
	fun sendShortLinkRequest(sendBody: ShortLinkRequest): Single<ResponseBody>
}
