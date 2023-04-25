package org.rfcx.incidents.view.guardian.checklist.microphone

import android.graphics.Color
import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.rfcx.incidents.R
import org.rfcx.incidents.databinding.FragmentGuardianMicrophoneBinding
import org.rfcx.incidents.util.spectrogram.MicrophoneTestUtils
import org.rfcx.incidents.util.spectrogram.SpectrogramListener
import org.rfcx.incidents.view.guardian.GuardianDeploymentEventListener
import org.rfcx.incidents.view.guardian.checklist.registration.GuardianRegisterViewModel
import java.util.*

class GuardianMicrophoneFragment : Fragment(), SpectrogramListener {

    private lateinit var binding: FragmentGuardianMicrophoneBinding
    private var spectrogramTimer: Timer? = null
    private var socketTimer: CountDownTimer? = null
    private val spectrogramStack = arrayListOf<FloatArray>()
    private var isTimerPause = false

    private lateinit var dialogBuilder: AlertDialog

    private val viewModel: GuardianMicrophoneViewModel by viewModel()
    private var mainEvent: GuardianDeploymentEventListener? = null

    private val microphoneTestUtils by lazy {
        MicrophoneTestUtils()
    }

    private var isMicTesting = false

    private var nullStackThreshold = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mainEvent = context as GuardianDeploymentEventListener
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_guardian_microphone, container, false)
        binding.lifecycleOwner = this
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.viewModel = viewModel

        mainEvent?.let {
            it.showToolbar()
            it.setToolbarTitle("Microphone Test")
        }

        checkTestingRequirement()

        setupAudioTrack()
        setupSpectrogram()
        setupSpectrogramSpeed()
        setupSpectrogramFreqMenu()
        setupSpectrogramColorMenu()
        setupAudioPlaybackMenu()
        setUiByState(MicTestingState.READY)
        AudioCastSocketManager.resetMicrophoneDefaultValue()

        binding.listenAudioButton.setOnClickListener {
            isMicTesting = true
            setUiByState(MicTestingState.LISTENING)
            retrieveLiveAudioBuffer()
        }

        binding.cancelAudioButton.setOnClickListener {
            isMicTesting = false
            isTimerPause = true
            setUiByState(MicTestingState.FINISH)
            spectrogramStack.clear()
            microphoneTestUtils.stop()
            stopSocketTimer()
        }

        binding.listenAgainAudioButton.setOnClickListener {
            isMicTesting = true
            isTimerPause = false
            setUiByState(MicTestingState.LISTENING)
            microphoneTestUtils.play()
            scheduleSocketTimer()
        }

        binding.finishButton.setOnClickListener {
            mainEvent?.next()
        }
    }

    private fun checkTestingRequirement() {
        deploymentProtocol?.getAudioCapturing()?.let {
            if (!it.isCapturing) {
                showAlert(it.msg ?: getString(R.string.unknown_error))
            }
        }
    }

    private fun showAlert(text: String) {
        val dialogBuilder =
            MaterialAlertDialogBuilder(requireContext(), R.style.BaseAlertDialog).apply {
                setTitle(null)
                setMessage(text)
                setPositiveButton(R.string.ok) { _, _ -> }
            }
        dialogBuilder.create().show()
    }

    private fun setupSpectrogram() {
        viewModel.resetSpectrogram()
        binding.spectrogramView.resetToDefaultValue()
        binding.spectrogramView.setSamplingRate(viewModel.sampleRateState.value)
        binding.spectrogramView.setBackgroundColor(Color.BLACK)
    }

    private fun setupSpectrogramSpeed() {
        binding.speedValueTextView.text = speed[0]
        binding.speedValueTextView.setOnClickListener {
            val builder = context?.let { it1 -> MaterialAlertDialogBuilder(it1, R.style.BaseAlertDialog) }
            if (builder != null) {
                builder.setTitle("Choose Speed")
                    .setItems(speed) { dialog, i ->
                        try {
                            binding.speedValueTextView.text = speed[i]
                            viewModel.setSpectrogramSpeed(speed[i])
                            viewModel.resetSpectrogramSetup()
                            spectrogramStack.clear()
                            binding.spectrogramView.invalidate()
                        } catch (e: IllegalArgumentException) {
                            dialog.dismiss()
                        }
                    }
                val dialog = builder.create()
                dialog.show()
            }
        }
    }

    private fun setupSpectrogramFreqMenu() {
        binding.freqScaleValueTextView.text = freq[0]
        binding.freqScaleValueTextView.setOnClickListener {
            val builder = context?.let { it1 -> MaterialAlertDialogBuilder(it1, R.style.BaseAlertDialog) }
            if (builder != null) {
                builder.setTitle("Choose Frequency")
                    .setItems(freq) { dialog, i ->
                        try {
                            binding.freqScaleValueTextView.text = freq[i]
                            binding.spectrogramView.freqScale = freq[i]
                            binding.spectrogramView.invalidate()
                        } catch (e: IllegalArgumentException) {
                            dialog.dismiss()
                        }
                    }
                val dialog = builder.create()
                dialog.show()
            }
        }
    }

    private fun setupSpectrogramColorMenu() {
        binding.colorSpecValueTextView.text = color[0]
        binding.colorSpecValueTextView.setOnClickListener {
            val builder = context?.let { it1 -> MaterialAlertDialogBuilder(it1, R.style.BaseAlertDialog) }
            if (builder != null) {
                builder.setTitle("Choose Color")
                    .setItems(color) { dialog, i ->
                        try {
                            binding.colorSpecValueTextView.text = color[i]
                            binding.spectrogramView.colorScale = color[i]
                            binding.spectrogramView.invalidate()
                        } catch (e: IllegalArgumentException) {
                            dialog.dismiss()
                        }
                    }
                val dialog = builder.create()
                dialog.show()
            }
        }
    }

    private fun setupAudioPlaybackMenu() {
        binding.playbackValueTextView.text = playback[0]
        binding.playbackValueTextView.setOnClickListener {
            val builder = context?.let { it1 -> MaterialAlertDialogBuilder(it1, R.style.BaseAlertDialog) }
            if (builder != null) {
                builder.setTitle("Choose Playback")
                    .setItems(playback) { dialog, i ->
                        try {
                            binding.playbackValueTextView.text = playback[i]
                            if (i == 0) {
                                viewModel.playAudio()
                            } else {
                                viewModel.stopAudio()
                            }
                        } catch (e: IllegalArgumentException) {
                            dialog.dismiss()
                        }
                    }
                val dialog = builder.create()
                dialog.show()
            }
        }
    }

    private fun setUiByState(state: MicTestingState) {
        when (state) {
            MicTestingState.READY -> {
                binding.listenAudioButton.visibility = View.VISIBLE
                binding.cancelAudioButton.visibility = View.GONE
                binding.listenAgainAudioButton.visibility = View.GONE
                binding.finishButton.visibility = View.GONE
            }
            MicTestingState.LISTENING -> {
                binding.listenAudioButton.visibility = View.GONE
                binding.cancelAudioButton.visibility = View.VISIBLE
                binding.listenAgainAudioButton.visibility = View.GONE
                binding.finishButton.visibility = View.GONE
            }
            MicTestingState.FINISH -> {
                binding.listenAudioButton.visibility = View.GONE
                binding.cancelAudioButton.visibility = View.GONE
                binding.listenAgainAudioButton.visibility = View.VISIBLE
                binding.finishButton.visibility = View.VISIBLE
            }
        }
    }

    private fun retrieveLiveAudioBuffer() {
        spectrogramTimer = Timer()

        AudioCastSocketManager.connect(microphoneTestUtils)

        spectrogramTimer?.schedule(
            object : TimerTask() {
                override fun run() {
                    if (!spectrogramStack.isNullOrEmpty()) {
                        nullStackThreshold = 0
                        try {
                            spectrogramView.setMagnitudes(spectrogramStack[0] ?: FloatArray(0))
                            spectrogramView.invalidate()
                            spectrogramStack.removeAt(0)
                        } catch (e: Exception) { /* nothing now */ }
                    } else {
                        nullStackThreshold++
                        if (nullStackThreshold >= 50) {
                            nullStackThreshold = 0
                            AudioCastSocketManager.connect(microphoneTestUtils)
                        }
                    }
                }
            },
            DELAY, STACK_PERIOD
        )

        setDialog()
        scheduleSocketTimer()

        AudioCastSocketManager.spectrogram.observe(
            viewLifecycleOwner,
            Observer {
                if (isMicTesting) {
                    if (it.size > 2) {
                        AudioSpectrogramUtils.setupSpectrogram(it.size)
                        val audioChunks = it.toShortArray().toSmallChunk(1)
                        for (chunk in audioChunks) {
                            AudioSpectrogramUtils.getTrunks(chunk, this)
                        }
                        stopSocketTimer()
                        scheduleSocketTimer()
                    }
                }
            }
        )
    }

    private fun setDialog() {
        dialogBuilder =
            MaterialAlertDialogBuilder(requireContext(), R.style.BaseAlertDialog).apply {
                setTitle(null)
                setMessage(R.string.dialog_start_service_mic)
                setPositiveButton(R.string.restart) { _, _ ->
                    GuardianSocketManager.restartService("audio-cast-socket")
                }
                setNegativeButton(R.string.back) { _, _ ->
                    dialogBuilder.dismiss()
                }
            }.create()
    }

    private fun scheduleSocketTimer() {
        socketTimer = object : CountDownTimer(10000, 1000) {
            override fun onTick(millisUntilFinished: Long) { }

            override fun onFinish() {
                showRestartGuardianServices()
                stopSocketTimer()
            }
        }
        socketTimer?.start()
    }

    private fun stopSocketTimer() {
        socketTimer?.cancel()
        socketTimer = null
    }

    override fun onProcessed(mag: FloatArray) {
        spectrogramStack.add(mag)
    }

    private fun showRestartGuardianServices() {
        if (::dialogBuilder.isInitialized && dialogBuilder.isShowing) {
            return
        }
        dialogBuilder.show()
    }

    override fun onDetach() {
        super.onDetach()
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
        AudioCastSocketManager.resetAllValuesToDefault()
        AudioCastSocketManager.stopConnection()
    }

    override fun onResume() {
        super.onResume()
        analytics?.trackScreen(Screen.GUARDIAN_MICROPHONE)
    }

    companion object {

        private val color = arrayOf("Rainbow", "Fire", "Ice", "Grey")
        private val freq = arrayOf("Linear", "Logarithmic")
        private val speed = arrayOf("Fast", "Normal", "Slow")
        private val playback = arrayOf("On", "Off")

        private const val DELAY = 0L

        private const val STACK_PERIOD = 10L

        private const val SOCKET_PERIOD = 120000L

        private const val DEF_SAMPLERATE = 12000

        enum class MicTestingState { READY, LISTENING, FINISH }

        fun newInstance(): GuardianMicrophoneFragment = GuardianMicrophoneFragment()
    }
}
