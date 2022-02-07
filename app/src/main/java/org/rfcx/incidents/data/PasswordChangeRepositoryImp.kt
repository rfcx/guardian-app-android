package org.rfcx.incidents.data

import io.reactivex.Single
import org.rfcx.incidents.data.interfaces.PasswordChangeRepository
import org.rfcx.incidents.data.remote.password.PasswordChangeEndpoint
import org.rfcx.incidents.entity.user.PasswordRequest
import org.rfcx.incidents.entity.user.PasswordResponse

class PasswordChangeRepositoryImp(private val passwordChangeEndpoint: PasswordChangeEndpoint) :
    PasswordChangeRepository {
    override fun newPassword(sendBody: PasswordRequest): Single<PasswordResponse> {
        return passwordChangeEndpoint.sendNewPassword(sendBody)
    }
}
