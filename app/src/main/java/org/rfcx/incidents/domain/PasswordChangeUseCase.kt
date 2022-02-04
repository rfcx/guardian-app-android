package org.rfcx.incidents.domain

import io.reactivex.Single
import org.rfcx.incidents.data.interfaces.PasswordChangeRepository
import org.rfcx.incidents.domain.base.SingleUseCase
import org.rfcx.incidents.domain.executor.PostExecutionThread
import org.rfcx.incidents.domain.executor.ThreadExecutor
import org.rfcx.incidents.entity.PasswordRequest
import org.rfcx.incidents.entity.PasswordResponse

class PasswordChangeUseCase(
    private val passwordChangeRepository: PasswordChangeRepository,
    threadExecutor: ThreadExecutor,
    postExecutionThread: PostExecutionThread
) : SingleUseCase<PasswordRequest, PasswordResponse>(threadExecutor, postExecutionThread) {
    override fun buildUseCaseObservable(params: PasswordRequest): Single<PasswordResponse> {
        return passwordChangeRepository.newPassword(params)
    }
}
