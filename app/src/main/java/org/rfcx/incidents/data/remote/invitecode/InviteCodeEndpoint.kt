package org.rfcx.incidents.data.remote.invitecode

import io.reactivex.Single
import org.rfcx.incidents.entity.user.InvitationCodeRequest
import org.rfcx.incidents.entity.user.InvitationCodeResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface InviteCodeEndpoint {
	
	@POST("v1/users/code")
	fun sendInvitationCode(@Body code: InvitationCodeRequest): Single<InvitationCodeResponse>
}
