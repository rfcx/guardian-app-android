package org.rfcx.incidents.data.remote.profilephoto

import io.reactivex.Single
import okhttp3.MultipartBody
import org.rfcx.incidents.data.remote.domain.SingleUseCase
import org.rfcx.incidents.data.remote.domain.executor.PostExecutionThread
import org.rfcx.incidents.data.remote.domain.executor.ThreadExecutor
import org.rfcx.incidents.entity.ProfilePhotoResponse

class ProfilePhotoUseCase(
    private val profilePhotoRepository: ProfilePhotoRepository,
    threadExecutor: ThreadExecutor, postExecutionThread: PostExecutionThread
) : SingleUseCase<MultipartBody.Part, ProfilePhotoResponse>(threadExecutor, postExecutionThread) {
    override fun buildUseCaseObservable(params: MultipartBody.Part): Single<ProfilePhotoResponse> {
        return profilePhotoRepository.sendProfilePhoto(params)
    }
}
