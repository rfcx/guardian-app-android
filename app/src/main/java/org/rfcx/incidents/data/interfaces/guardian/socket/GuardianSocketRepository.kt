package org.rfcx.incidents.data.interfaces.guardian.socket

import android.net.wifi.ScanResult
import kotlinx.coroutines.flow.Flow
import org.rfcx.incidents.data.remote.common.Result

interface GuardianSocketRepository {
    fun initialize(): Flow<Result<Boolean>>
    fun getMessage(): Flow<Result<String>>
    fun sendMessage(message: String): Flow<Result<Boolean>>
}
