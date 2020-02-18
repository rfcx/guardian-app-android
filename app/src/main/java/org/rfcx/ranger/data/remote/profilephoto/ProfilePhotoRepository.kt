package org.rfcx.ranger.data.remote.profilephoto

import io.reactivex.Single
import okhttp3.MultipartBody
import org.rfcx.ranger.entity.ProfilePhotoResponse

interface ProfilePhotoRepository {
	fun sendProfilePhoto(sendBody: MultipartBody.Part): Single<ProfilePhotoResponse>
}