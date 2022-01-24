package org.rfcx.incidents.data.remote.invitecode

import io.reactivex.Single
import org.rfcx.incidents.data.remote.domain.SingleUseCase
import org.rfcx.incidents.data.remote.domain.executor.PostExecutionThread
import org.rfcx.incidents.data.remote.domain.executor.ThreadExecutor
import org.rfcx.incidents.entity.user.InvitationCodeRequest
import org.rfcx.incidents.entity.user.InvitationCodeResponse

class SendInviteCodeUseCase(private val inviteCodeRepository: InviteCodeRepository,
                            threadExecutor: ThreadExecutor, postExecutionThread: PostExecutionThread
) : SingleUseCase<InvitationCodeRequest, InvitationCodeResponse>(threadExecutor, postExecutionThread) {
	override fun buildUseCaseObservable(params: InvitationCodeRequest): Single<InvitationCodeResponse> {
		return inviteCodeRepository.sendInviteCode(params)
	}
}
