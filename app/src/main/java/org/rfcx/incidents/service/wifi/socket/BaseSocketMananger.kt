package org.rfcx.incidents.service.wifi.socket

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.shareIn
import org.rfcx.incidents.data.remote.common.Result
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.EOFException
import java.net.Socket

@OptIn(ExperimentalCoroutinesApi::class)
abstract class BaseSocketMananger {

    private val scope = CoroutineScope(Dispatchers.IO)

    private var socket: Socket? = null
    private var readChannel: DataInputStream? = null
    private var writeChannel: DataOutputStream? = null

    private var port: Int = 0
    private var fromInit = false

    private lateinit var messageSharedFlow: SharedFlow<Result<String>>

    private val _messageShared = MutableSharedFlow<Result<String>>(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    val messageShared = _messageShared.asSharedFlow()

    enum class Type {
        GUARDIAN, ADMIN, ALL
    }

    fun initialize(port: Int): Flow<Result<Boolean>> {
        this.port = port
        return flow {
            try {
                // Need to send a message to establish the connection
                // To emit loading to UI
                fromInit = true
                send("{}")
                emit(Result.Success(true))
            } catch (e: Exception) {
                if (isErrorNeedReset(e)) {
                    emit(Result.Error(e))
                }
            }
        }
    }

    fun send(message: String) {
        // Always need to new Socket object to re-send message
        socket = Socket("192.168.43.1", port)
        socket?.keepAlive = true
        socket?.soTimeout = 10000
        writeChannel = DataOutputStream(socket!!.getOutputStream())
        writeChannel?.writeUTF(message)
        writeChannel?.flush()
    }

    fun read(): Flow<Result<String>> {
        return callbackFlow {
            if (fromInit) {
                trySendBlocking(Result.Loading)
                _messageShared.tryEmit(Result.Loading)
            }
            var isActive = true
            try {
                while (isActive) {
                    readChannel = DataInputStream(socket!!.getInputStream())
                    val dataInput = readChannel?.readUTF()
                    if (dataInput != null) {
                        trySendBlocking(Result.Success(dataInput))
                        _messageShared.tryEmit(Result.Success(dataInput))
                        Log.d("Comp", dataInput.toString())
                    }
                }
            } catch (e: Exception) {
                if (isErrorNeedReset(e)) {
                    trySendBlocking(Result.Error(e))
                    _messageShared.tryEmit(Result.Error(e))
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
