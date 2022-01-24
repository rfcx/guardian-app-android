package org.rfcx.incidents.data.remote.invitecode

import io.reactivex.Single
import org.rfcx.incidents.entity.user.InvitationCodeRequest
import org.rfcx.incidents.entity.user.InvitationCodeResponse

interface InviteCodeRepository {
	fun sendInviteCode(sendInviteBody: InvitationCodeRequest): Single<InvitationCodeResponse>
}
