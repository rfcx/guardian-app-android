package org.rfcx.ranger.data.remote.password

import io.reactivex.Single
import org.rfcx.ranger.entity.PasswordRequest
import org.rfcx.ranger.entity.PasswordResponse

class PasswordChangeRepositoryImp(private val passwordChangeEndpoint: PasswordChangeEndpoint) : PasswordChangeRepository {
	override fun newPassword(sendBody: PasswordRequest): Single<PasswordResponse> {
		return passwordChangeEndpoint.sendNewPassword(sendBody)
	}
}