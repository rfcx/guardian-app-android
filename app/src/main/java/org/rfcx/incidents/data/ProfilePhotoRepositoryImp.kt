package org.rfcx.incidents.data

import io.reactivex.Single
import okhttp3.MultipartBody
import org.rfcx.incidents.data.remote.profilephoto.ProfilePhotoEndpoint
import org.rfcx.incidents.entity.ProfilePhotoResponse

class ProfilePhotoRepositoryImp(private val profilePhotoEndpoint: ProfilePhotoEndpoint) : ProfilePhotoRepository {
    override fun sendProfilePhoto(sendBody: MultipartBody.Part): Single<ProfilePhotoResponse> {
        return profilePhotoEndpoint.sendImageProfile(sendBody)
    }
}
