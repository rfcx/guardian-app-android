package org.rfcx.ranger.data.remote.password

import io.reactivex.Single
import org.rfcx.ranger.data.remote.domain.SingleUseCase
import org.rfcx.ranger.data.remote.domain.executor.PostExecutionThread
import org.rfcx.ranger.data.remote.domain.executor.ThreadExecutor
import org.rfcx.ranger.entity.PasswordRequest
import org.rfcx.ranger.entity.PasswordResponse

class PasswordChangeUseCase(private val passwordChangeRepository: PasswordChangeRepository,
                            threadExecutor: ThreadExecutor, postExecutionThread: PostExecutionThread
) : SingleUseCase<PasswordRequest, PasswordResponse>(threadExecutor, postExecutionThread) {
	override fun buildUseCaseObservable(params: PasswordRequest): Single<PasswordResponse> {
		return passwordChangeRepository.newPassword(params)
	}
}
