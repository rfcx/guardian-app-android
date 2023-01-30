package org.rfcx.incidents.data.guardian.software

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.shareIn
import org.rfcx.incidents.data.interfaces.guardian.software.SoftwareRepository
import org.rfcx.incidents.data.local.guardian.GuardianFileDb
import org.rfcx.incidents.data.remote.common.Result
import org.rfcx.incidents.data.remote.guardian.software.SoftwareEndpoint
import org.rfcx.incidents.data.remote.guardian.software.SoftwareResponse
import org.rfcx.incidents.entity.guardian.GuardianFile

class SoftwareRepositoryImpl(
    private val endpoint: SoftwareEndpoint,
    private val localDb: GuardianFileDb
) : SoftwareRepository {
    override fun getRemote(): Flow<Result<List<SoftwareResponse>>> {
        return flow {
            emit(Result.Loading)
            emit(Result.Success(endpoint.getSoftware()))
        }
    }

    override fun getLocal(): Flow<Result<List<GuardianFile>>> {
        return flow {
            emit(Result.Loading)
            emit(Result.Success(localDb.getAll()))
        }
    }

    override fun download(url: String): Flow<Result<List<GuardianFile>>> {
        return flow {}
    }
}
