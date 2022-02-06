package org.rfcx.incidents.data.interfaces

import io.reactivex.Single
import okhttp3.MultipartBody
import org.rfcx.incidents.entity.user.ProfilePhotoResponse

interface ProfilePhotoRepository {
    fun sendProfilePhoto(sendBody: MultipartBody.Part): Single<ProfilePhotoResponse>
}
