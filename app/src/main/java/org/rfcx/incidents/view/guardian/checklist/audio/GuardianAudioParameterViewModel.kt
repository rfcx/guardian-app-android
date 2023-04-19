package org.rfcx.incidents.view.guardian.checklist.audio

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.JsonObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.rfcx.incidents.domain.guardian.socket.GetGuardianMessageUseCase
import org.rfcx.incidents.domain.guardian.socket.InstructionParams
import org.rfcx.incidents.domain.guardian.socket.SendInstructionCommandUseCase
import org.rfcx.incidents.entity.guardian.TimeRange
import org.rfcx.incidents.entity.guardian.socket.InstructionCommand
import org.rfcx.incidents.entity.guardian.socket.InstructionType
import org.rfcx.incidents.util.socket.PingUtils.getAudioParameter
import org.rfcx.incidents.util.socket.PingUtils.getPrefsSha1
import org.rfcx.incidents.util.socket.PrefsUtils
import org.rfcx.incidents.util.time.toGuardianFormat

class GuardianAudioParameterViewModel(
    private val getGuardianMessageUseCase: GetGuardianMessageUseCase,
    private val sendInstructionCommandUseCase: SendInstructionCommandUseCase
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
    private val _loadingState: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val loadingState = _loadingState.asStateFlow()

    private val _prefsSyncState: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val prefsSyncState = _prefsSyncState.asStateFlow()

    private var sampleRate = 24000 // default guardian sampleRate is 24000
    private var bitrate = 28672 // default guardian bitrate is 28672
    private var fileFormat = "opus" // default guardian file format is opus
    private var duration = 90 // default guardian duration is 90
    var enableSampling = false
    var sampling = "1:2"
    var schedule = "23:55-23:56,23:57-23:59"

    private var needCheckSha1 = false
    private var currentGuardianSha1 = ""
    private var isFirstTime = true

    init {
        getAudioParameter()
        getPrefsSha1()
    }

    private fun getAudioParameter() {
        viewModelScope.launch {
            getGuardianMessageUseCase.launch().catch {

            }.collectLatest { result ->
                result?.getAudioParameter()?.let {
                    if (isFirstTime) {
                        sampleRate = it.get(PrefsUtils.audioSampleRate).asInt
                        bitrate = it.get(PrefsUtils.audioBitrate).asInt
                        fileFormat = it.get(PrefsUtils.audioCodec).asString
                        duration = it.get(PrefsUtils.audioDuration).asInt
                        enableSampling = it.get(PrefsUtils.enableSampling).asBoolean
                        sampling = it.get(PrefsUtils.sampling).asString.last().toString()
                        schedule = it.get(PrefsUtils.schedule).asString
                        showSampleRate()
                        showBitrate()
                        showFileFormat()
                        showDuration()
                        showSampling()
                        isFirstTime = false
                    }
                }
            }
        }
    }

    private fun getPrefsSha1() {
        viewModelScope.launch {
            getGuardianMessageUseCase.launch().catch {

            }.collectLatest { result ->
                result?.getPrefsSha1()?.let {
                    if (needCheckSha1 && currentGuardianSha1 != it) {
                        _prefsSyncState.tryEmit(true)
                        needCheckSha1 = false
                    }
                    currentGuardianSha1 = it
                }
            }
        }
    }

    fun syncParameter(schedule: List<TimeRange>) {
        val scheduleGuardianFormat = schedule.toGuardianFormat()
        if (this.schedule != scheduleGuardianFormat) {
            needCheckSha1 = true
        }
        this.schedule = if (schedule.isEmpty()) "23:55-23:56,23:57-23:59" else scheduleGuardianFormat
        if (!needCheckSha1) {
            _prefsSyncState.tryEmit(true)
        } else {
            viewModelScope.launch(Dispatchers.IO) {
                _loadingState.tryEmit(true)
                sendInstructionCommandUseCase.launch(InstructionParams(InstructionType.SET, InstructionCommand.PREFS, getAsPrefsFormat()))
            }
        }
    }

    private fun getAsPrefsFormat(): String {
        val json = JsonObject().apply {
            addProperty(PrefsUtils.audioSampleRate, sampleRate)
            addProperty(PrefsUtils.audioCastSampleRate, sampleRate)
            addProperty(PrefsUtils.audioBitrate, bitrate)
            addProperty(PrefsUtils.audioCodec, fileFormat)
            addProperty(PrefsUtils.audioDuration, duration)
            addProperty(PrefsUtils.enableSampling, enableSampling)
            addProperty(PrefsUtils.sampling, sampling)
            addProperty(PrefsUtils.schedule, schedule)
        }
        return json.toString()
    }

    fun selectFileFormat(value: String) {
        if (value != fileFormat) {
            needCheckSha1 = true
        }
        fileFormat = value
        showFileFormat()
    }

    private fun showFileFormat() {
        _fileFormatTextState.tryEmit(fileFormat)
    }

    fun selectSampleRate(value: String) {
        if (value.toInt() != sampleRate) {
            needCheckSha1 = true
        }
        sampleRate = value.toInt()
        showSampleRate()
    }

    private fun showSampleRate() {
        _sampleRateTextState.tryEmit("${(sampleRate / 1000)} kHz")
    }

    fun selectDuration(value: String) {
        if (value.toInt() != duration) {
            needCheckSha1 = true
        }
        duration = value.toInt()
        showDuration()
    }

    private fun showDuration() {
        _durationTextState.tryEmit("$duration secs")
    }

    fun selectBitrate(value: String) {
        if (value.toInt() != bitrate) {
            needCheckSha1 = true
        }
        bitrate = value.toInt()
        showBitrate()
    }

    private fun showBitrate() {
        _bitrateTextState.tryEmit("${(bitrate / 1000)} kbps")
    }

    fun selectSampling(value: String) {
        val tempSamplingRatio = "1:$value"
        when {
            value == "0" && !enableSampling -> needCheckSha1 = false
            tempSamplingRatio == sampling && enableSampling -> needCheckSha1 = false
            tempSamplingRatio == sampling && !enableSampling -> {
                enableSampling = true
                needCheckSha1 = true
            }
            else -> {
                if (value != "0") {
                    sampling = tempSamplingRatio
                }
                enableSampling = value != "0"
                needCheckSha1 = true
            }
        }
        showSampling()
    }

    private fun showSampling() {
        if (!enableSampling) {
            _samplingTextState.tryEmit("0")
        } else {
            _samplingTextState.tryEmit(sampling.last().toString())
        }
    }
}
