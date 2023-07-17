package org.rfcx.incidents.data.guardian.socket

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import org.rfcx.incidents.data.interfaces.guardian.socket.AudioSocketRepository
import org.rfcx.incidents.data.remote.common.Result
import org.rfcx.incidents.service.wifi.socket.AudioSocket

class AudioSocketRepositoryImpl(
    private val audioSocket: AudioSocket
) : AudioSocketRepository {
    override fun initialize(): Flow<Result<Boolean>> {
        return audioSocket.initialize(9998)
    }

    override fun getMessage(): Flow<Result<String>> {
        return audioSocket.read()
    }

    override fun getMessageSharedFlow(): SharedFlow<String> {
        return audioSocket.messageShared
    }

    override fun sendMessage(message: String) {
        audioSocket.send(message)
    }

    override fun close() {
        audioSocket.close()
    }
}
