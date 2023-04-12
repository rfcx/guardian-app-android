package org.rfcx.incidents.view.guardian.checklist.audio

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.rfcx.incidents.domain.guardian.socket.GetGuardianMessageUseCase
import org.rfcx.incidents.util.socket.PingUtils.getAudioParameter
import org.rfcx.incidents.util.socket.PrefsUtils

class GuardianAudioParameterViewModel(
    private val getGuardianMessageUseCase: GetGuardianMessageUseCase
) : ViewModel() {

    private val _sampleRateTextState: MutableStateFlow<String> = MutableStateFlow("")
    val sampleRateTextState = _sampleRateTextState.asStateFlow()
    private val _bitrateTextState: MutableStateFlow<String> = MutableStateFlow("")
    val bitrateTextState = _bitrateTextState.asStateFlow()
    private val _fileFormatTextState: MutableStateFlow<String> = MutableStateFlow("")
    val fileFormatTextState = _fileFormatTextState.asStateFlow()
    private val _durationTextState: MutableStateFlow<String> = MutableStateFlow("")
    val durationTextState = _durationTextState.asStateFlow()
    private val _samplingTextState: MutableStateFlow<String> = MutableStateFlow("")
    val samplingTextState = _samplingTextState.asStateFlow()
    private val _scheduleTextState: MutableStateFlow<String> = MutableStateFlow("")
    val scheduleTextState = _scheduleTextState.asStateFlow()

    private var sampleRate = 24000 // default guardian sampleRate is 24000
    private var bitrate = 28672 // default guardian bitrate is 28672
    private var fileFormat = "opus" // default guardian file format is opus
    private var duration = 90 // default guardian duration is 90
    private var enableSampling = false
    private var sampling = "1:2"
    private var schedule = "23:55-23:56,23:57-23:59"

    init {
        getAudioParameter()
    }

    private fun getAudioParameter() {
        viewModelScope.launch {
            getGuardianMessageUseCase.launch().catch {

            }.collectLatest { result ->
                result?.getAudioParameter()?.let {
                    sampleRate = it.get(PrefsUtils.audioSampleRate).asInt
                    bitrate = it.get(PrefsUtils.audioBitrate).asInt
                    fileFormat = it.get(PrefsUtils.audioCodec).asString
                    duration = it.get(PrefsUtils.audioDuration).asInt
                    enableSampling = it.get(PrefsUtils.enableSampling).asBoolean
                    sampling = it.get(PrefsUtils.sampling).asString
                    schedule = it.get(PrefsUtils.schedule).asString

                    _sampleRateTextState.tryEmit(sampleRate.toString())
                    _bitrateTextState.tryEmit(bitrate.toString())
                    _fileFormatTextState.tryEmit(fileFormat)
                    _durationTextState.tryEmit(duration.toString())
                    _samplingTextState.tryEmit(sampling)
                    _scheduleTextState.tryEmit(schedule)
                }
            }
        }
    }
}
