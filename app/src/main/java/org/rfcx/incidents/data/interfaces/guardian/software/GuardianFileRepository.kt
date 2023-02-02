package org.rfcx.incidents.data.interfaces.guardian.software

import kotlinx.coroutines.flow.Flow
import org.rfcx.incidents.data.remote.common.Result
import org.rfcx.incidents.data.remote.guardian.software.ClassifierResponse
import org.rfcx.incidents.data.remote.guardian.software.SoftwareResponse
import org.rfcx.incidents.entity.guardian.GuardianFile

interface GuardianFileRepository {
    fun getSoftwareRemote(): Flow<Result<List<SoftwareResponse>>>
    fun getClassifierRemote(): Flow<Result<List<ClassifierResponse>>>
    fun getSoftwareLocalAsFlow(): Flow<List<GuardianFile>>
    fun getClassifierLocalAsFlow(): Flow<List<GuardianFile>>
    fun download(targetFile: GuardianFile): Flow<Result<Boolean>>
    fun delete(targetFile: GuardianFile): Flow<Result<Boolean>>
}
