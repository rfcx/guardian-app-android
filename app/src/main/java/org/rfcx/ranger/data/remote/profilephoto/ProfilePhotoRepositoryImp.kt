package org.rfcx.ranger.data.remote.profilephoto

import io.reactivex.Single
import okhttp3.MultipartBody
import org.rfcx.ranger.entity.ProfilePhotoResponse

class ProfilePhotoRepositoryImp(private val profilePhotoEndpoint: ProfilePhotoEndpoint) : ProfilePhotoRepository {
	override fun sendProfilePhoto(sendBody: MultipartBody.Part): Single<ProfilePhotoResponse> {
		return profilePhotoEndpoint.sendImageProfile(sendBody)
	}
}