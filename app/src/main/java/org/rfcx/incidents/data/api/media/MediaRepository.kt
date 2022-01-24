package org.rfcx.incidents.data.api.media

import io.reactivex.Single
import okhttp3.ResponseBody

interface MediaRepository {
	fun sendFilename(filename: String): Single<ResponseBody>
}
