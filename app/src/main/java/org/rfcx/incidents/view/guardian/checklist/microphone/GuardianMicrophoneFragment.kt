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
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.rfcx.incidents.R
import org.rfcx.incidents.databinding.FragmentGuardianMicrophoneBinding
import org.rfcx.incidents.util.spectrogram.MicrophoneTestUtils
import org.rfcx.incidents.util.spectrogram.SpectrogramListener
import org.rfcx.incidents.view.guardian.GuardianDeploymentEventListener
import org.rfcx.incidents.view.guardian.checklist.registration.GuardianRegisterViewModel
import java.util.*

class GuardianMicrophoneFragment : Fragment() {

    private lateinit var binding: FragmentGuardianMicrophoneBinding
    private val spectrogramStack = arrayListOf<FloatArray>()
    private var isTimerPause = false

    private lateinit var dialogBuilder: AlertDialog

    private val viewModel: GuardianMicrophoneViewModel by viewModel()
    private var mainEvent: GuardianDeploymentEventListener? = null

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

        setupSpectrogram()
        setupSpectrogramSpeed()
        setupSpectrogramFreqMenu()
        setupSpectrogramColorMenu()
        setupAudioPlaybackMenu()
        setUiByState(MicTestingState.READY)

        binding.listenAudioButton.setOnClickListener {
            viewModel.setMicTesting(true)
            setUiByState(MicTestingState.LISTENING)
            retrieveLiveAudioBuffer()
        }

        binding.cancelAudioButton.setOnClickListener {
            viewModel.setMicTesting(false)
            setUiByState(MicTestingState.FINISH)
            viewModel.onHaltClicked()
        }

        binding.listenAgainAudioButton.setOnClickListener {
            viewModel.setMicTesting(true)
            setUiByState(MicTestingState.LISTENING)
            viewModel.onResumedClicked()
        }

        binding.finishButton.setOnClickListener {
            mainEvent?.next()
        }
    }

    private fun checkTestingRequirement() {
        lifecycleScope.launch {
            viewModel.warnDisableDialogState.collectLatest {
                if (it.first) {
                    showAlert(it.second ?: getString(R.string.unknown_error))
                }
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
        viewModel.getAudio()
        lifecycleScope.launch {
            viewModel.spectrogramState.collectLatest {
                binding.spectrogramView.setMagnitudes(it)
                binding.spectrogramView.invalidate()
            }
        }
        setDialog()
    }

    private fun setDialog() {
        dialogBuilder =
            MaterialAlertDialogBuilder(requireContext(), R.style.BaseAlertDialog).apply {
                setTitle(null)
                setMessage("microphone...")
                setPositiveButton(R.string.restart) { _, _ ->
                    viewModel.restartAudioService()
                }
                setNegativeButton(R.string.back) { _, _ ->
                    dialogBuilder.dismiss()
                }
            }.create()

        lifecycleScope.launch {
            viewModel.warnEmptyDialogState.collectLatest {
                if (it) {
                    showRestartGuardianServices()
                }
            }
        }
    }

    private fun showRestartGuardianServices() {
        if (::dialogBuilder.isInitialized && dialogBuilder.isShowing) {
            return
        }
        dialogBuilder.show()
    }

    override fun onDetach() {
        super.onDetach()
        viewModel.onDestroy()
    }

    companion object {

        private val color = arrayOf("Rainbow", "Fire", "Ice", "Grey")
        private val freq = arrayOf("Linear", "Logarithmic")
        private val speed = arrayOf("Fast", "Normal", "Slow")
        private val playback = arrayOf("On", "Off")
        enum class MicTestingState { READY, LISTENING, FINISH }
        fun newInstance(): GuardianMicrophoneFragment = GuardianMicrophoneFragment()
    }
}
