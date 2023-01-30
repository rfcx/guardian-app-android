package org.rfcx.incidents.data.guardian.software

import android.util.Log
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.rfcx.incidents.data.interfaces.guardian.software.SoftwareRepository
import org.rfcx.incidents.data.local.guardian.GuardianFileDb
import org.rfcx.incidents.data.remote.common.Result
import org.rfcx.incidents.data.remote.guardian.software.SoftwareEndpoint
import org.rfcx.incidents.data.remote.guardian.software.SoftwareResponse
import org.rfcx.incidents.entity.guardian.GuardianFile
import org.rfcx.incidents.service.guardianfile.GuardianFileHelper

class SoftwareRepositoryImpl(
    private val endpoint: SoftwareEndpoint,
    private val localDb: GuardianFileDb,
    private val helper: GuardianFileHelper
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

    override fun download(targetFile: GuardianFile): Flow<Result<Boolean>> {
        return flow {
            emit(Result.Loading)
            val result = endpoint.downloadFile(targetFile.url)
            val writeResult = helper.saveToDisk(result, targetFile)

            if (writeResult is Result.Success) {
                val file = targetFile
                file.path = writeResult.data
                localDb.save(file)
                emit(Result.Success(true))
            }
            if (writeResult is Result.Error) {
                emit(Result.Error(writeResult.throwable))
            }
        }
    }

    override fun delete(targetFile: GuardianFile): Flow<Result<Boolean>> {
        return flow {
            emit(Result.Loading)
            val result = helper.removeFromDisk(targetFile)
            if (result is Result.Success) {
                localDb.delete(targetFile)
                emit(Result.Success(true))
            }
            if (result is Result.Error) {
                emit(Result.Error(result.throwable))
            }
        }
    }
}
