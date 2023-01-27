package org.rfcx.incidents.data.interfaces.guardian.software

import kotlinx.coroutines.flow.Flow
import org.rfcx.incidents.data.remote.common.Result
import org.rfcx.incidents.data.remote.guardian.software.SoftwareResponse
import org.rfcx.incidents.entity.guardian.Software

interface SoftwareRepository {
    fun getRemote(): Flow<Result<List<SoftwareResponse>>>
    fun getLocal(): Flow<Result<List<Software>>>
    fun download(url: String): Flow<Result<List<Software>>>
}
