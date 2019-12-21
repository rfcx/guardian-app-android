package org.rfcx.ranger.data.remote.shortlink

import io.reactivex.Single
import okhttp3.ResponseBody
import org.rfcx.ranger.entity.shortlink.ShortLinkRequest

interface ShortLinkRepository {
	fun sendShortLinkRequest(sendBody: ShortLinkRequest): Single<ResponseBody>
}