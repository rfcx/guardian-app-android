package org.rfcx.incidents.data.remote.media

import io.reactivex.Single
import okhttp3.ResponseBody

class MediaRepositoryImp(private val assetsEndpoint: MediaEndpoint) : MediaRepository {
    override fun sendFilename(filename: String): Single<ResponseBody> {
        return assetsEndpoint.filename(filename)
    }
}
