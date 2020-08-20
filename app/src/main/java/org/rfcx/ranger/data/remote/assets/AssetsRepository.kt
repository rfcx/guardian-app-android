package org.rfcx.ranger.data.remote.assets

import io.reactivex.Single
import okhttp3.ResponseBody

interface AssetsRepository {
	fun sendFilename(filename: String): Single<ResponseBody>
}
