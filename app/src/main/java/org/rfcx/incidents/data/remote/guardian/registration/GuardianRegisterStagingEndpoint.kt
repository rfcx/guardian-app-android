package org.rfcx.incidents.data.remote.guardian.registration

import org.rfcx.incidents.entity.guardian.registration.GuardianRegisterRequest
import retrofit2.http.Body
import retrofit2.http.POST

interface GuardianRegisterStagingEndpoint {
    @POST("guardians")
    suspend fun register(@Body registration: GuardianRegisterRequest): GuardianRegisterResponse
}
