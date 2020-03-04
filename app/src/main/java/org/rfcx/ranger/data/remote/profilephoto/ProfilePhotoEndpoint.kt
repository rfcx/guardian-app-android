package org.rfcx.ranger.data.remote.profilephoto

import io.reactivex.Single
import okhttp3.MultipartBody
import org.rfcx.ranger.entity.ProfilePhotoResponse
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface ProfilePhotoEndpoint {
	@POST("v1/users/avatar-change")
	@Multipart
	fun sendImageProfile(@Part() imageFile: MultipartBody.Part): Single<ProfilePhotoResponse>
}