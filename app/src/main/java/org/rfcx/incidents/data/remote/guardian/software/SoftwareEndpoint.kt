package org.rfcx.incidents.data.remote.guardian.software

import retrofit2.http.GET

interface SoftwareEndpoint {
    @GET("v2/guardians/software/all")
    suspend fun getSoftware(): List<SoftwareResponse>
}
