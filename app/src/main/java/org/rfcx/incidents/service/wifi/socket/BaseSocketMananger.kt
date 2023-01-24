package org.rfcx.incidents.service.wifi.socket

import android.util.Log
import com.google.gson.Gson
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import org.rfcx.incidents.data.remote.common.Result
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.EOFException
import java.net.Socket

@OptIn(ExperimentalCoroutinesApi::class)
abstract class BaseSocketMananger {

    private var socket: Socket? = null
    private var readChannel: DataInputStream? = null
    private var writeChannel: DataOutputStream? = null

    private var port: Int = 0
    var isAvailableToSend = false

    data class SocketRequest(
        val command: String
    )

    fun initialize(port: Int): Flow<Result<Boolean>> {
        this.port = port
        return send(Gson().toJson(SocketRequest("connection")))
    }

    fun send(message: String): Flow<Result<Boolean>> {
        return flow {
            try {
                socket = Socket("192.168.43.1", port)
                socket?.keepAlive = true
                socket?.soTimeout = 10000
                readChannel = DataInputStream(socket!!.getInputStream())
                writeChannel = DataOutputStream(socket!!.getOutputStream())
                isAvailableToSend = true

                writeChannel?.writeUTF(message)
                writeChannel?.flush()

                emit(Result.Success(true))
            } catch (e: Exception) {
                if (isErrorNeedReset(e)) {
                    emit(Result.Error(e))
                }
            }
        }
    }

    fun read(): Flow<Result<String>> {
        return callbackFlow {
            trySendBlocking(Result.Loading)
            try {
                while (true) {
                    val dataInput = readChannel?.readUTF()
                    if (dataInput != null) {
                        trySendBlocking(Result.Success(dataInput))
                        Log.d("Comp", dataInput.toString())
                    }
                }
            } catch (e: Exception) {
                if (isErrorNeedReset(e)) {
                    trySendBlocking(Result.Error(e))
                }
            }

            awaitClose {
                close()
            }
        }
    }

    private fun isErrorNeedReset(e: Exception): Boolean {
        if (e is EOFException) return false
        val message = e.message
        message?.let {
            when (it.lowercase()) {
                "broken pipe" -> isAvailableToSend = false
                "connection reset" -> isAvailableToSend = false
                "Read timed out" -> isAvailableToSend = false
            }
        }
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
