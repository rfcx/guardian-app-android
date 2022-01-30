package org.rfcx.incidents.data.remote.password

import io.reactivex.Single
import org.rfcx.incidents.entity.PasswordRequest
import org.rfcx.incidents.entity.PasswordResponse

interface PasswordChangeRepository {
    fun newPassword(sendBody: PasswordRequest): Single<PasswordResponse>
}
