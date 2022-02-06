package org.rfcx.incidents.data.interfaces

import io.reactivex.Single
import org.rfcx.incidents.entity.user.PasswordRequest
import org.rfcx.incidents.entity.user.PasswordResponse

interface PasswordChangeRepository {
    fun newPassword(sendBody: PasswordRequest): Single<PasswordResponse>
}
