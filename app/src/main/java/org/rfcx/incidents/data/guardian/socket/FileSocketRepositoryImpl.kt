package org.rfcx.incidents.data.guardian.socket

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import org.rfcx.incidents.data.interfaces.guardian.socket.FileSocketRepository
import org.rfcx.incidents.data.remote.common.Result
import org.rfcx.incidents.entity.guardian.file.GuardianFile
import org.rfcx.incidents.entity.guardian.file.GuardianFileSendStatus
import org.rfcx.incidents.service.wifi.socket.FileSocket

class FileSocketRepositoryImpl(
    private val fileSocket: FileSocket
) : FileSocketRepository {
    override fun sendFile(file: GuardianFile): Flow<Result<GuardianFileSendStatus>> {
        return fileSocket.sendFile(file)
    }

    override fun initialize(): Flow<Result<Boolean>> {
        return fileSocket.initialize(9996)
    }

    override fun getMessage(): Flow<Result<String>> {
        return fileSocket.read()
    }

    override fun getMessageSharedFlow(): SharedFlow<String> {
        return fileSocket.messageShared
    }

    override fun sendMessage(message: String) {
        return fileSocket.send(message)
    }

    override fun close() {
        return fileSocket.close()
    }
}
