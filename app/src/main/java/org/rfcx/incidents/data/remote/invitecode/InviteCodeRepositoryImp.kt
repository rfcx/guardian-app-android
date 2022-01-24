package org.rfcx.incidents.data.remote.invitecode

import io.reactivex.Single
import org.rfcx.incidents.entity.user.InvitationCodeRequest
import org.rfcx.incidents.entity.user.InvitationCodeResponse

class InviteCodeRepositoryImp(private val inviteCodeEndpoint: InviteCodeEndpoint) : InviteCodeRepository {
	
	override fun sendInviteCode(sendInviteBody: InvitationCodeRequest): Single<InvitationCodeResponse> {
		return inviteCodeEndpoint.sendInvitationCode(sendInviteBody)
	}
}
