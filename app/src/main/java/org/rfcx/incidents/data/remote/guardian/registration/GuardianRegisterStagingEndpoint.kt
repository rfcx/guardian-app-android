package org.rfcx.incidents.data.remote.guardian.registration

import org.rfcx.incidents.entity.guardian.registration.GuardianRegisterRequest
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface GuardianRegisterStagingEndpoint {
    @POST("guardians")
    suspend fun registerSuspend(@Body registration: GuardianRegisterRequest): GuardianRegisterResponse

    @POST("guardians")
    fun register(@Body registration: GuardianRegisterRequest): Call<GuardianRegisterResponse>
}
