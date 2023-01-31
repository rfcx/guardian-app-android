package org.rfcx.incidents.data.remote.guardian.software

import retrofit2.http.GET

interface ClassifierEndpoint {
    @GET("classifiers")
    suspend fun getClassifier(): List<ClassifierResponse>
}
