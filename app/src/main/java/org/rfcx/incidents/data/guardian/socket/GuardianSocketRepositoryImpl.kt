package org.rfcx.incidents.data.guardian.socket

import android.util.Log
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
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

    override fun sendMessage(message: String) {
        guardianSocket.send(message)
    }

    override fun close() {
        guardianSocket.close()
    }
}
