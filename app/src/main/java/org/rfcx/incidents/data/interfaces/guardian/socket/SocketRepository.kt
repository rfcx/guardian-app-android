package org.rfcx.incidents.data.interfaces.guardian.socket

import kotlinx.coroutines.flow.Flow
import org.rfcx.incidents.data.remote.common.Result

interface SocketRepository {
    fun initialize(): Flow<Result<Boolean>>
    fun getMessage(): Flow<Result<String>>
    fun sendMessage(message: String)
    fun close()
}
