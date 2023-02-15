package org.rfcx.incidents.domain.guardian.socket

import android.util.Log
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.rfcx.incidents.data.interfaces.guardian.socket.GuardianSocketRepository
import org.rfcx.incidents.data.remote.common.Result
import org.rfcx.incidents.domain.base.FlowUseCase
import org.rfcx.incidents.entity.guardian.socket.GuardianPing
import org.rfcx.incidents.util.socket.PingUtils

class GetGuardianMessageUseCase(private val guardianRepository: GuardianSocketRepository) : FlowUseCase<GuardianPing?>() {
    override fun performAction(): Flow<GuardianPing?> {
        Log.d("Comp7", "waiting")
        return guardianRepository.getMessageSharedFlow().map { result ->
            when (result) {
                is Result.Success -> {
                    val gson = Gson()
                    try {
                        gson.fromJson(PingUtils.unGzipString(result.data), GuardianPing::class.java)
                    } catch (e: Exception){
                        null
                    }
                }
                else -> null
            }
        }
    }
}
