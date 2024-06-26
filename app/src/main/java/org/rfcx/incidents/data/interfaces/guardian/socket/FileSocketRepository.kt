package org.rfcx.incidents.data.interfaces.guardian.socket

import kotlinx.coroutines.flow.Flow
import org.rfcx.incidents.data.remote.common.Result
import org.rfcx.incidents.entity.guardian.file.GuardianFile
import org.rfcx.incidents.entity.guardian.file.GuardianFileSendStatus

interface FileSocketRepository : SocketRepository {
    fun sendFile(file: GuardianFile): Flow<Result<GuardianFileSendStatus>>
}
