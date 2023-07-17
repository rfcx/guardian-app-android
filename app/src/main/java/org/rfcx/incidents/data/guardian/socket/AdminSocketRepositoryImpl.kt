package org.rfcx.incidents.data.guardian.socket

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import org.rfcx.incidents.data.interfaces.guardian.socket.AdminSocketRepository
import org.rfcx.incidents.data.remote.common.Result
import org.rfcx.incidents.service.wifi.socket.AdminSocket

class AdminSocketRepositoryImpl(
    private val adminSocket: AdminSocket
) : AdminSocketRepository {
    override fun initialize(): Flow<Result<Boolean>> {
        return adminSocket.initialize(9997)
    }

    override fun getMessage(): Flow<Result<String>> {
        return adminSocket.read()
    }

    override fun getMessageSharedFlow(): SharedFlow<String> {
        return adminSocket.messageShared
    }

    override fun sendMessage(message: String) {
        adminSocket.send(message)
    }

    override fun close() {
        adminSocket.close()
    }
}
