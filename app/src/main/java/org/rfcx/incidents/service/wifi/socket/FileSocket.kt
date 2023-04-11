package org.rfcx.incidents.service.wifi.socket

import android.os.SystemClock
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import org.rfcx.incidents.data.remote.common.Result
import org.rfcx.incidents.entity.guardian.GuardianFile
import org.rfcx.incidents.entity.guardian.GuardianFileSendStatus
import java.io.DataOutputStream
import java.io.File
import java.net.Socket
import kotlin.math.roundToInt

object FileSocket : BaseSocketManager() {

    fun sendFile(guardianFile: GuardianFile): Flow<Result<GuardianFileSendStatus>> {
        return flow {
            emit(Result.Loading)
            try {
                socket = Socket("192.168.43.1", 9996)
                socket?.keepAlive = true
                writeChannel = DataOutputStream(socket?.getOutputStream())

                val file = File(guardianFile.path)
                val fileSize = file.length()
                var progress = 0
                val buffer = ByteArray(8192)
                var count: Int
                val inp = file.inputStream()
                writeChannel?.write(file.name.toByteArray())
                writeChannel?.write("|".toByteArray())

                if (guardianFile.meta.isNotEmpty()) {
                    writeChannel?.write(guardianFile.meta.toByteArray())
                }

                writeChannel?.write("|".toByteArray())

                while (true) {
                    count = inp.read(buffer)
                    progress += count
                    if (count < 0) {
                        break
                    }
                    writeChannel?.write(buffer, 0, count)
                    emit(Result.Success(GuardianFileSendStatus(false, ((progress.toDouble() / fileSize.toDouble()) * 100).roundToInt())))
                }

                writeChannel?.flush()

                SystemClock.sleep(5000)

                writeChannel?.write("****".toByteArray())
                writeChannel?.flush()
                emit(Result.Success(GuardianFileSendStatus(true, 100)))
            } catch (e: Exception) {
                throw e
            }
        }
            .flowOn(Dispatchers.IO)
    }
}
