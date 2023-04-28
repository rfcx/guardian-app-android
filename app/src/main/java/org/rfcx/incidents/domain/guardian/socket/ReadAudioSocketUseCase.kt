package org.rfcx.incidents.domain.guardian.socket

import kotlinx.coroutines.flow.Flow
import org.rfcx.incidents.data.interfaces.guardian.socket.AudioSocketRepository
import org.rfcx.incidents.data.remote.common.Result
import org.rfcx.incidents.domain.base.FlowUseCase

class ReadAudioSocketUseCase(private val audioSocketRepository: AudioSocketRepository): FlowUseCase<Result<String>>() {
    override fun performAction(): Flow<Result<String>> {
        return audioSocketRepository.getMessage()
    }
}
