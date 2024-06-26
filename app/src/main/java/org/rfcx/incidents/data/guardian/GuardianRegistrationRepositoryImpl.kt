package org.rfcx.incidents.data.guardian

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import org.rfcx.incidents.data.interfaces.guardian.GuardianRegistrationRepository
import org.rfcx.incidents.data.local.guardian.GuardianRegistrationDb
import org.rfcx.incidents.data.remote.common.Result
import org.rfcx.incidents.data.remote.guardian.registration.GuardianRegisterProductionEndpoint
import org.rfcx.incidents.data.remote.guardian.registration.GuardianRegisterResponse
import org.rfcx.incidents.data.remote.guardian.registration.GuardianRegisterStagingEndpoint
import org.rfcx.incidents.entity.guardian.registration.GuardianRegisterRequest
import org.rfcx.incidents.entity.guardian.registration.GuardianRegistration

class GuardianRegistrationRepositoryImpl(
    private val productionEndpoint: GuardianRegisterProductionEndpoint,
    private val stagingEndpoint: GuardianRegisterStagingEndpoint,
    private val localDb: GuardianRegistrationDb
) : GuardianRegistrationRepository {
    override fun saveRegistration(registration: GuardianRegistration): Flow<Boolean> {
        return try {
            localDb.save(registration)
            flowOf(true)
        } catch (e: Exception) {
            flowOf(false)
        }
    }

    override fun sendRegistrationOnline(env: String, registration: GuardianRegisterRequest): Flow<Result<GuardianRegisterResponse>> {
        return flow {
            emit(Result.Loading)
            localDb.markSending(registration.guid)
            if (env == "production") {
                emit(Result.Success(productionEndpoint.registerSuspend(registration)))
                localDb.markSent(registration.guid)
            } else {
                emit(Result.Success(stagingEndpoint.registerSuspend(registration)))
                localDb.markSent(registration.guid)
            }
        }.catch { e ->
            emit(Result.Error(e))
            localDb.markUnsent(registration.guid)
        }
    }

    override fun list(): Flow<List<GuardianRegistration>> {
        return localDb.listAsFlow()
    }
}
