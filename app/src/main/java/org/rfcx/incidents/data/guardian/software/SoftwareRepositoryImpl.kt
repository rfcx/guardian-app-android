package org.rfcx.incidents.data.guardian.software

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.rfcx.incidents.data.interfaces.guardian.software.SoftwareRepository
import org.rfcx.incidents.data.remote.common.Result
import org.rfcx.incidents.data.remote.guardian.software.SoftwareEndpoint
import org.rfcx.incidents.data.remote.guardian.software.SoftwareResponse
import org.rfcx.incidents.entity.guardian.Software

class SoftwareRepositoryImpl(
    private val endpoint: SoftwareEndpoint
) : SoftwareRepository {
    override fun getRemote(): Flow<Result<List<SoftwareResponse>>> {
        return flow {
            emit(Result.Loading)
            val software = endpoint.getSoftware()
            emit(Result.Success(software))
        }
    }

    override fun getLocal(): Flow<Result<List<Software>>> {
        return flow {}
    }

    override fun download(url: String): Flow<Result<List<Software>>> {
        return flow {}
    }
}
