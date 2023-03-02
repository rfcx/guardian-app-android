package org.rfcx.incidents.domain.guardian.socket

import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.rfcx.incidents.data.interfaces.guardian.socket.AdminSocketRepository
import org.rfcx.incidents.domain.base.FlowUseCase
import org.rfcx.incidents.entity.guardian.socket.AdminPing
import org.rfcx.incidents.util.socket.PingUtils

class GetAdminMessageUseCase(private val adminRepository: AdminSocketRepository) : FlowUseCase<AdminPing?>() {
    override fun performAction(): Flow<AdminPing?> {
        return adminRepository.getMessageSharedFlow().map { result ->
            val gson = Gson()
            try {
                gson.fromJson(PingUtils.unGzipString(result), AdminPing::class.java)
            } catch (e: Exception) {
                null
            }
        }
    }
}
