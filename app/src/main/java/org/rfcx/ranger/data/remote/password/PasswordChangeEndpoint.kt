package org.rfcx.ranger.data.remote.password

import io.reactivex.Single
import org.rfcx.ranger.entity.PasswordRequest
import org.rfcx.ranger.entity.PasswordResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface PasswordChangeEndpoint {
	@POST("v1/users/password-change")
	fun sendNewPassword(@Body password: PasswordRequest): Single<PasswordResponse>
}