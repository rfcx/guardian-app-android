package org.rfcx.incidents.data

import io.reactivex.Single
import okhttp3.ResponseBody
import org.rfcx.incidents.data.interfaces.MediaRepository
import org.rfcx.incidents.data.remote.media.MediaEndpoint

class MediaRepositoryImp(private val assetsEndpoint: MediaEndpoint) : MediaRepository {
    override fun sendFilename(filename: String): Single<ResponseBody> {
        return assetsEndpoint.filename(filename)
    }
}
