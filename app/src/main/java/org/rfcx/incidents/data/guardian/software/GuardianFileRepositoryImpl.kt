package org.rfcx.incidents.data.guardian.software

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.rfcx.incidents.data.interfaces.guardian.software.GuardianFileRepository
import org.rfcx.incidents.data.local.guardian.GuardianFileDb
import org.rfcx.incidents.data.remote.common.Result
import org.rfcx.incidents.data.remote.guardian.software.ClassifierEndpoint
import org.rfcx.incidents.data.remote.guardian.software.ClassifierResponse
import org.rfcx.incidents.data.remote.guardian.software.SoftwareEndpoint
import org.rfcx.incidents.data.remote.guardian.software.SoftwareResponse
import org.rfcx.incidents.entity.guardian.GuardianFile
import org.rfcx.incidents.entity.guardian.GuardianFileType
import org.rfcx.incidents.service.guardianfile.GuardianFileHelper

class GuardianFileRepositoryImpl(
    private val softwareEndpoint: SoftwareEndpoint,
    private val classifierEndpoint: ClassifierEndpoint,
    private val localDb: GuardianFileDb,
    private val helper: GuardianFileHelper
) : GuardianFileRepository {
    override fun getSoftwareRemote(): Flow<Result<List<SoftwareResponse>>> {
        return flow {
            emit(Result.Loading)
            try {
                emit(Result.Success(softwareEndpoint.getSoftware()))
            } catch (e: Exception) {
                emit(Result.Error(e))
            }
        }
    }

    override fun getClassifierRemote(): Flow<Result<List<ClassifierResponse>>> {
        return flow {
            emit(Result.Loading)
            try {
                emit(Result.Success(classifierEndpoint.getClassifier()))
            } catch (e: Exception) {
                emit(Result.Error(e))
            }
        }
    }

    override fun getLocal(): List<GuardianFile> {
        return localDb.getAll()
    }

    override fun getLocalAsFlow(): Flow<List<GuardianFile>> {
        return localDb.getAllAsync()
    }

    override fun download(targetFile: GuardianFile): Flow<Result<Boolean>> {
        return flow {
            emit(Result.Loading)
            val result = softwareEndpoint.downloadFile(targetFile.url)
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
