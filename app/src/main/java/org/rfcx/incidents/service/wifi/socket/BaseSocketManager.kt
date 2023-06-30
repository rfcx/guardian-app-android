package org.rfcx.incidents.service.wifi.socket

import android.util.Log
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import org.rfcx.incidents.data.remote.common.Result
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.EOFException
import java.net.Socket

abstract class BaseSocketManager {

    var socket: Socket? = null
    var readChannel: DataInputStream? = null
    var writeChannel: DataOutputStream? = null

    var port: Int = 0
    var fromInit = false

    private val _messageShared = MutableSharedFlow<String>(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    val messageShared = _messageShared.asSharedFlow()

    enum class Type {
        GUARDIAN, ADMIN, FILE, AUDIO, ALL
    }

    fun initialize(port: Int): Flow<Result<Boolean>> {
        this.port = port
        return flow {
            try {
                // Need to send a message to establish the connection
                // To emit loading to UI
                Log.d("GuardianApp", "init $port")
                fromInit = true
                send("{\"command\":\"connection0\"}")
                emit(Result.Success(true))
                Log.d("GuardianApp", "sent init $port")
            } catch (e: Exception) {
                if (isErrorNeedReset(e)) {
                    emit(Result.Error(e))
                }
            }
        }
    }

    fun send(message: String) {
        // Always need to new Socket object to re-send message
        try {
            socket = Socket("192.168.43.1", port)
            socket?.keepAlive = true
            socket?.soTimeout = 10000
            writeChannel = DataOutputStream(socket!!.getOutputStream())
            writeChannel?.writeUTF(message)
            writeChannel?.flush()
        } catch (_: Exception) {
        }
    }

    fun read(): Flow<Result<String>> {
        return callbackFlow {
            if (fromInit) {
                trySendBlocking(Result.Loading)
                fromInit = false
            }
            var isActive = true
            try {
                while (isActive) {
                    readChannel = DataInputStream(socket!!.getInputStream())
                    val dataInput = readChannel?.readUTF()
                    if (dataInput != null) {
                        trySendBlocking(Result.Success(dataInput))
                        _messageShared.tryEmit(dataInput)
                    }
                }
            } catch (e: Exception) {
                if (isErrorNeedReset(e)) {
                    trySendBlocking(Result.Error(e))
                }
            }

            awaitClose {
                isActive = false
            }
        }
    }

    private fun isErrorNeedReset(e: Exception): Boolean {
        if (e is EOFException) return false
        return true
    }

    fun close() {
        writeChannel?.close()
        readChannel?.close()
        socket?.close()

        writeChannel = null
        readChannel = null
        socket = null
    }
}
