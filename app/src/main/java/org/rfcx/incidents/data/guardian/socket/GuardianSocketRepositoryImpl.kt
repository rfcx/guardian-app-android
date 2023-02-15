package org.rfcx.incidents.data.guardian.socket

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import org.rfcx.incidents.data.interfaces.guardian.socket.GuardianSocketRepository
import org.rfcx.incidents.data.remote.common.Result
import org.rfcx.incidents.service.wifi.socket.GuardianSocket

class GuardianSocketRepositoryImpl(
    private val guardianSocket: GuardianSocket
): GuardianSocketRepository {
    override fun initialize(): Flow<Result<Boolean>> {
        return guardianSocket.initialize(9999)
    }

    override fun getMessage(): Flow<Result<String>> {
        return guardianSocket.read()
    }

    override fun getMessageSharedFlow(): SharedFlow<Result<String>> {
        return guardianSocket.messageShared
    }

    override fun sendMessage(message: String) {
        guardianSocket.send(message)
    }

    override fun close() {
        guardianSocket.close()
    }
}
