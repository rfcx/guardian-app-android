package org.rfcx.incidents.data.remote.password

import io.reactivex.Single
import org.rfcx.incidents.entity.PasswordRequest
import org.rfcx.incidents.entity.PasswordResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface PasswordChangeEndpoint {
    @POST("v1/users/password-change")
    fun sendNewPassword(@Body password: PasswordRequest): Single<PasswordResponse>
}
