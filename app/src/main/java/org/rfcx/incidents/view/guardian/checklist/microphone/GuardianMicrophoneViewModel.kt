package org.rfcx.incidents.view.guardian.checklist.microphone

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.rfcx.incidents.domain.guardian.socket.GetAudioMessageUseCase
import org.rfcx.incidents.domain.guardian.socket.GetGuardianMessageUseCase
import org.rfcx.incidents.util.spectrogram.MicrophoneTestUtils
import org.rfcx.incidents.view.guardian.checklist.CheckListItem
import java.util.PrimitiveIterator

class GuardianMicrophoneViewModel(
    private val getGuardianMessageUseCase: GetGuardianMessageUseCase,
    private val getAudioMessageUseCase: GetAudioMessageUseCase,
    private val microphoneTestUtils: MicrophoneTestUtils
): ViewModel() {

    private var audioChunks = arrayListOf<String>()
    private var tempAudio = ByteArray(0)
    private var isTestingFirstTime = true

    private val _spectrogramState: MutableStateFlow<String> = MutableStateFlow("")
    val spectrogramState = _spectrogramState.asStateFlow()

    init {
        getAudio()
    }

    fun getAudio() {
        viewModelScope.launch {
            getAudioMessageUseCase.launch().catch {

            }.collectLatest { result ->
                result?.let { audio ->
                    audioChunks.add(audio.buffer)
                    if (audio.amount == audio.number) {
                        var fullAudio = ByteArray(0)

                        audioChunks
                            .map { microphoneTestUtils.decodeEncodedAudio(it) }
                            .forEach { fullAudio += it!! }

                        if (isTestingFirstTime) {
                            microphoneTestUtils.let { util ->
                                util.init(fullAudio.size)
                                util.play()
                            }
                            isTestingFirstTime = false
                        }
                        if (!tempAudio.contentEquals(fullAudio)) {
                            tempAudio = fullAudio
                            microphoneTestUtils.let {
                                it.buffer = fullAudio
                                it.setTrack()
                                _spectrogramState.tryEmit(audio.buffer)
                            }
                        }
                        audioChunks.clear()
                    }
                }
            }
        }
    }
}
