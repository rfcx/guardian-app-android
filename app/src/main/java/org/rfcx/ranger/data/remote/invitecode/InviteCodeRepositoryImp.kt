package org.rfcx.ranger.data.remote.invitecode

import io.reactivex.Single
import org.rfcx.ranger.entity.user.InvitationCodeRequest
import org.rfcx.ranger.entity.user.InvitationCodeResponse

class InviteCodeRepositoryImp(private val inviteCodeEndpoint: InviteCodeEndpoint) : InviteCodeRepository {
	
	override fun sendInviteCode(sendInviteBody: InvitationCodeRequest): Single<InvitationCodeResponse> {
		return inviteCodeEndpoint.sendInvitationCode(sendInviteBody)
	}
}