package org.rfcx.incidents.data

import io.reactivex.Single
import okhttp3.MultipartBody
import org.rfcx.incidents.entity.ProfilePhotoResponse

interface ProfilePhotoRepository {
    fun sendProfilePhoto(sendBody: MultipartBody.Part): Single<ProfilePhotoResponse>
}
