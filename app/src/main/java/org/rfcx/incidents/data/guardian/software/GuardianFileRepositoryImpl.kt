package org.rfcx.incidents.data.guardian.software

import com.google.firebase.crashlytics.FirebaseCrashlytics
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import org.rfcx.incidents.data.interfaces.guardian.software.GuardianFileRepository
import org.rfcx.incidents.data.local.guardian.GuardianFileDb
import org.rfcx.incidents.data.remote.common.Result
import org.rfcx.incidents.data.remote.guardian.software.ClassifierEndpoint
import org.rfcx.incidents.data.remote.guardian.software.ClassifierResponse
import org.rfcx.incidents.data.remote.guardian.software.DownloadFileEndpoint
import org.rfcx.incidents.data.remote.guardian.software.SoftwareEndpoint
import org.rfcx.incidents.data.remote.guardian.software.SoftwareResponse
import org.rfcx.incidents.entity.guardian.file.GuardianFile
import org.rfcx.incidents.service.guardianfile.GuardianFileHelper

class GuardianFileRepositoryImpl(
    private val softwareEndpoint: SoftwareEndpoint,
    private val classifierEndpoint: ClassifierEndpoint,
    private val downloadFileEndpoint: DownloadFileEndpoint,
    private val localDb: GuardianFileDb,
    private val helper: GuardianFileHelper
) : GuardianFileRepository {
    override fun getSoftwareRemote(): Flow<Result<List<SoftwareResponse>>> {
        return flow {
            emit(Result.Loading)
            emit(Result.Success(softwareEndpoint.getSoftware()))
        }.catch { e ->
            emit(Result.Error(e))
        }
    }

    override fun getClassifierRemote(): Flow<Result<List<ClassifierResponse>>> {
        return flow {
            emit(Result.Loading)
            emit(Result.Success(classifierEndpoint.getClassifier()))
        }.catch { e ->
            emit(Result.Error(e))
        }
    }

    override fun getSoftwareLocalAsFlow(): Flow<List<GuardianFile>> {
        return localDb.getSoftwareAllAsync()
    }

    override fun getClassifierLocalAsFlow(): Flow<List<GuardianFile>> {
        return localDb.getClassifierAllAsync()
    }

    override fun download(targetFile: GuardianFile): Flow<Result<Boolean>> {
        return flow {
            emit(Result.Loading)
            // Any endpoint is file for download
            withContext(Dispatchers.IO) {
                val result = downloadFileEndpoint.downloadFile(targetFile.url)
                val writeResult = helper.saveToDisk(result, targetFile)
                targetFile.path = writeResult
            }
            localDb.save(targetFile)
            emit(Result.Success(true))
        }.catch { e ->
            FirebaseCrashlytics.getInstance().recordException(e)
            emit(Result.Error(Throwable(e.stackTraceToString())))
        }
    }

    override fun delete(targetFile: GuardianFile): Flow<Result<Boolean>> {
        return flow {
            emit(Result.Loading)
            helper.removeFromDisk(targetFile)
            localDb.delete(targetFile)
            emit(Result.Success(true))
        }.catch { e ->
            emit(Result.Error(e))
        }
    }
}
