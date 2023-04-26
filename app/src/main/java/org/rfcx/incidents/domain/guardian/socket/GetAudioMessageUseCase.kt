package org.rfcx.incidents.domain.guardian.socket

import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.rfcx.incidents.data.interfaces.guardian.socket.AudioSocketRepository
import org.rfcx.incidents.domain.base.FlowUseCase
import org.rfcx.incidents.entity.guardian.socket.AudioPing

class GetAudioMessageUseCase(private val audioSocketRepository: AudioSocketRepository) : FlowUseCase<AudioPing?>() {
    override fun performAction(): Flow<AudioPing?> {
        return audioSocketRepository.getMessageSharedFlow().map { result ->
            val gson = Gson()
            try {
                gson.fromJson(result, AudioPing::class.java)
            } catch (e: Exception) {
                null
            }
        }
    }
}
