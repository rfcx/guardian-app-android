package org.rfcx.ranger.data.remote.password

import io.reactivex.Single
import org.rfcx.ranger.entity.PasswordRequest
import org.rfcx.ranger.entity.PasswordResponse

interface PasswordChangeRepository {
	fun newPassword(sendBody: PasswordRequest): Single<PasswordResponse>
}