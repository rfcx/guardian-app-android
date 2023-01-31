package org.rfcx.incidents.data.interfaces.guardian.software

import kotlinx.coroutines.flow.Flow
import okhttp3.ResponseBody
import org.rfcx.incidents.data.remote.common.Result
import org.rfcx.incidents.data.remote.guardian.software.SoftwareResponse
import org.rfcx.incidents.entity.guardian.GuardianFile

interface SoftwareRepository {
    fun getRemote(): Flow<Result<List<SoftwareResponse>>>
    fun getLocal(): List<GuardianFile>
    fun getLocalAsFlow(): Flow<List<GuardianFile>>
    fun download(targetFile: GuardianFile): Flow<Result<Boolean>>
    fun delete(targetFile: GuardianFile): Flow<Result<Boolean>>
}
