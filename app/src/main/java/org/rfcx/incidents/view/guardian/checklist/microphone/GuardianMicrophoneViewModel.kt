package org.rfcx.incidents.view.guardian.checklist.microphone

import android.os.CountDownTimer
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.JsonObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.rfcx.incidents.data.remote.common.Result
import org.rfcx.incidents.domain.guardian.socket.CloseSocketParams
import org.rfcx.incidents.domain.guardian.socket.CloseSocketUseCase
import org.rfcx.incidents.domain.guardian.socket.GetAudioMessageUseCase
import org.rfcx.incidents.domain.guardian.socket.GetGuardianMessageUseCase
import org.rfcx.incidents.domain.guardian.socket.InitAudioSocketUseCase
import org.rfcx.incidents.domain.guardian.socket.InitSocketUseCase
import org.rfcx.incidents.domain.guardian.socket.InstructionParams
import org.rfcx.incidents.domain.guardian.socket.ReadAudioSocketUseCase
import org.rfcx.incidents.domain.guardian.socket.SendInstructionCommandUseCase
import org.rfcx.incidents.entity.guardian.socket.InstructionCommand
import org.rfcx.incidents.entity.guardian.socket.InstructionType
import org.rfcx.incidents.service.wifi.socket.BaseSocketManager
import org.rfcx.incidents.util.socket.PingUtils.getAudioCaptureStatus
import org.rfcx.incidents.util.socket.PingUtils.getAudioParameter
import org.rfcx.incidents.util.socket.PingUtils.getSampleRate
import org.rfcx.incidents.util.spectrogram.AudioSpectrogramUtils
import org.rfcx.incidents.util.spectrogram.MicrophoneTestUtils
import org.rfcx.incidents.util.spectrogram.SpectrogramListener
import org.rfcx.incidents.util.spectrogram.toShortArray
import org.rfcx.incidents.util.spectrogram.toSmallChunk
import org.rfcx.incidents.view.guardian.checklist.CheckListItem
import java.util.PrimitiveIterator
import java.util.Timer
import java.util.TimerTask

class GuardianMicrophoneViewModel(
    private val initSocketUseCase: InitAudioSocketUseCase,
    private val readAudioSocketUseCase: ReadAudioSocketUseCase,
    private val getGuardianMessageUseCase: GetGuardianMessageUseCase,
    private val getAudioMessageUseCase: GetAudioMessageUseCase,
    private val sendInstructionCommandUseCase: SendInstructionCommandUseCase,
    private val closeSocketUseCase: CloseSocketUseCase,
    private val microphoneTestUtils: MicrophoneTestUtils,
    private val audioSpectrogramUtils: AudioSpectrogramUtils
): ViewModel() {

    private var audioChunks = arrayListOf<String>()
    private var tempAudio = ByteArray(0)
    private var isTestingFirstTime = true

    private val _spectrogramState: MutableStateFlow<FloatArray> = MutableStateFlow(FloatArray(0))
    val spectrogramState = _spectrogramState.asStateFlow()
    private val _sampleRateState: MutableStateFlow<Int> = MutableStateFlow(12000)
    val sampleRateState = _sampleRateState.asStateFlow()
    private val _warnEmptyDialogState: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val warnEmptyDialogState = _warnEmptyDialogState.asStateFlow()
    private val _warnDisableDialogState: MutableStateFlow<Pair<Boolean, String?>> = MutableStateFlow(Pair(false, null))
    val warnDisableDialogState = _warnDisableDialogState.asStateFlow()

    private var spectrogramTimer: Timer? = null
    private var socketTimer: CountDownTimer? = null
    private val spectrogramStack = arrayListOf<FloatArray>()
    private var isMicTesting = false
    private var nullStackThreshold = 0

    init {
        initAudioSocket()
        getAudioConfig()
    }

    fun setMicTesting(isTesting: Boolean) {
        isMicTesting = isTesting
    }

    private fun initAudioSocket() {
        viewModelScope.launch(Dispatchers.IO) {
            initSocketUseCase.launch().collectLatest { result ->
                when(result) {
                    is Result.Error -> {}
                    Result.Loading -> {}
                    is Result.Success -> {
                        readAudioSocketUseCase.launch().collect()
                    }
                }
            }
        }
    }

    fun setSpectrogramSize(size: Int) {
        audioSpectrogramUtils.setupSpectrogram(size)
    }

    fun setSpectrogramSpeed(speed: String) {
        audioSpectrogramUtils.setSpeed(speed)
    }

    fun resetSpectrogramSetup() {
        audioSpectrogramUtils.resetSetupState()
    }

    fun resetSpectrogram() {
        audioSpectrogramUtils.resetToDefaultValue()
    }

    fun playAudio() {
        microphoneTestUtils.play()
    }

    fun stopAudio() {
        microphoneTestUtils.stop()
    }

    fun getAudioConfig() {
        viewModelScope.launch {
            getGuardianMessageUseCase.launch().catch {

            }.collectLatest { result ->
                result?.getSampleRate()?.let {
                    if (_sampleRateState.value == -1) {
                        _sampleRateState.tryEmit(it)
                        microphoneTestUtils.setSampleRate(_sampleRateState.value)
                    }
                }
                result?.getAudioCaptureStatus()?.let {
                    if (!it.isCapturing) {
                        _warnDisableDialogState.tryEmit(Pair(true, it.msg))
                    }
                }
            }
        }
    }

    fun getAudio() {
        notifySpectrogram()
        viewModelScope.launch {
            getAudioMessageUseCase.launch().catch {

            }.collectLatest { result ->
                result?.let { audio ->
                    Log.d("Comp", result.buffer)
                    audioChunks.add(audio.buffer)
                    if (audio.amount == audio.number) {
                        var fullAudio = ByteArray(0)

                        audioChunks
                            .map { microphoneTestUtils.decodeEncodedAudio(it) }
                            .forEach { fullAudio += it }

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
                                setSpectrogram(it.buffer)
                            }
                        }
                        audioChunks.clear()
                    }
                }
            }
        }
    }

    private fun setSpectrogram(buffer: ByteArray) {
        if (isMicTesting) {
            if (buffer.size > 2) {
                setSpectrogramSize(buffer.size)
                val audioChunks = buffer.toShortArray().toSmallChunk(1)
                for (chunk in audioChunks) {
                    audioSpectrogramUtils.getTrunks(chunk, object: SpectrogramListener {
                        override fun onProcessed(mag: FloatArray) {
                            spectrogramStack.add(mag)
                        }
                    })
                }
                stopSocketTimer()
                scheduleSocketTimer()
            }
        }
    }

    private fun notifySpectrogram() {
        spectrogramTimer = Timer()
        spectrogramTimer?.schedule(
            object : TimerTask() {
                override fun run() {
                    if (spectrogramStack.isNotEmpty()) {
                        nullStackThreshold = 0
                        try {
                            _spectrogramState.tryEmit(spectrogramStack[0])
                            spectrogramStack.removeAt(0)
                        } catch (e: Exception) { /* nothing now */ }
                    } else {
                        nullStackThreshold++
                        if (nullStackThreshold >= 500) {
                            nullStackThreshold = 0
                            initAudioSocket()
                        }
                    }
                }
            },
            0L, 10L
        )
    }

    private fun scheduleSocketTimer() {
        socketTimer = object : CountDownTimer(10000, 1000) {
            override fun onTick(millisUntilFinished: Long) { }

            override fun onFinish() {
                _warnEmptyDialogState.tryEmit(true)
                stopSocketTimer()
            }
        }
        socketTimer?.start()
    }

    fun restartAudioService() {
        viewModelScope.launch(Dispatchers.IO) {
            val json = JsonObject()
            json.addProperty("service", "audio-cast-socket")
            sendInstructionCommandUseCase.launch(InstructionParams(InstructionType.CTRL, InstructionCommand.RESTART, Gson().toJson(json)))
        }
    }

    private fun stopSocketTimer() {
        _warnEmptyDialogState.tryEmit(false)
        socketTimer?.cancel()
        socketTimer = null
    }

    fun onHaltClicked() {
        spectrogramStack.clear()
        stopAudio()
        stopSocketTimer()
    }

    fun onResumedClicked() {
        playAudio()
        scheduleSocketTimer()
    }

    fun onDestroy() {
        microphoneTestUtils.let {
            it.stop()
            it.release()
        }
        spectrogramTimer?.cancel()
        spectrogramTimer = null

        stopSocketTimer()

        if (isMicTesting) {
            isMicTesting = false
        }

        viewModelScope.launch {
            closeSocketUseCase.launch(CloseSocketParams(BaseSocketManager.Type.AUDIO))
        }
    }
}
