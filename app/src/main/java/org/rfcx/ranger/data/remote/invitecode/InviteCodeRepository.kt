package org.rfcx.ranger.data.remote.invitecode

import io.reactivex.Single
import org.rfcx.ranger.entity.user.InvitationCodeRequest
import org.rfcx.ranger.entity.user.InvitationCodeResponse

interface InviteCodeRepository {
	fun sendInviteCode(sendInviteBody: InvitationCodeRequest): Single<InvitationCodeResponse>
}