package org.rfcx.incidents.domain.guardian.socket

import android.util.Log
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.rfcx.incidents.data.interfaces.guardian.socket.GuardianSocketRepository
import org.rfcx.incidents.data.remote.common.Result
import org.rfcx.incidents.domain.base.FlowUseCase
import org.rfcx.incidents.entity.guardian.socket.GuardianPing

class GetGuardianMessageUseCase(private val guardianRepository: GuardianSocketRepository) : FlowUseCase<GuardianPing?>() {
    override fun performAction(): Flow<GuardianPing?> {
        return guardianRepository.getMessage().map { result ->
            Log.d("Comp7", result.toString())
            when (result) {
                is Result.Success -> {
                    val gson = Gson()
                    gson.fromJson(result.data, GuardianPing::class.java)
                }
                else -> null
            }
        }
    }
}
