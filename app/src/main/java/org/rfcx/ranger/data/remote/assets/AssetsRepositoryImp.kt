package org.rfcx.ranger.data.remote.assets

import io.reactivex.Single
import okhttp3.ResponseBody

class AssetsRepositoryImp(private val assetsEndpoint: AssetsEndpoint) : AssetsRepository {
	override fun sendFilename(filename: String): Single<ResponseBody> {
		return assetsEndpoint.filename(filename)
	}
}
