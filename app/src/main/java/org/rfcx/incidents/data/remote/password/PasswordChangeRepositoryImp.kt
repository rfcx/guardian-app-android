package org.rfcx.incidents.data.remote.password

import io.reactivex.Single
import org.rfcx.incidents.entity.PasswordRequest
import org.rfcx.incidents.entity.PasswordResponse

class PasswordChangeRepositoryImp(private val passwordChangeEndpoint: PasswordChangeEndpoint) :
    PasswordChangeRepository {
    override fun newPassword(sendBody: PasswordRequest): Single<PasswordResponse> {
        return passwordChangeEndpoint.sendNewPassword(sendBody)
    }
}
