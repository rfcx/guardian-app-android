package org.rfcx.incidents.view.guardian.checklist.audio

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.rfcx.incidents.R
import org.rfcx.incidents.databinding.FragmentGuardianAudioParameterBinding
import org.rfcx.incidents.view.guardian.GuardianDeploymentEventListener
import org.rfcx.incidents.widget.NumberPickerButtonClickListener
import org.rfcx.incidents.widget.NumberPickerDialog

class GuardianAudioParameterFragment : Fragment(), NumberPickerButtonClickListener {

    private lateinit var binding: FragmentGuardianAudioParameterBinding
    private val viewModel: GuardianAudioParameterViewModel by viewModel()

    private var mainEvent: GuardianDeploymentEventListener? = null

    // Predefined configuration values
    private var sampleRateEntries: Array<String>? = null
    private var sampleRateValues: Array<String>? = null
    private var bitrateEntries: Array<String>? = null
    private var bitrateValues: Array<String>? = null
    private var fileFormatList: Array<String>? = null
    private var durationEntries: Array<String>? = null
    private var durationValues: Array<String>? = null
    private var samplingEntries: Array<String>? = null
    private var samplingValues: Array<String>? = null

    private var sampleRate = 24000 // default guardian sampleRate is 24000
    private var bitrate = 28672 // default guardian bitrate is 28672
    private var fileFormat = "opus" // default guardian file format is opus
    private var duration = 90 // default guardian duration is 90
    private var enableSampling = false
    private var sampling = "1:2"
    private var schedule = "23:55-23:56,23:57-23:59"

    private var needCheckSha1 = false
    private var currentPrefsSha1: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mainEvent = context as GuardianDeploymentEventListener
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_guardian_audio_parameter, container, false)
        binding.lifecycleOwner = this
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.viewModel = viewModel

        mainEvent?.let {
            it.showToolbar()
            it.setToolbarTitle("Audio Parameter Config")
        }

        setPredefinedConfiguration(requireContext())
        setFileFormatLayout()
        setSampleRateLayout()
        setBitrateLayout()
        setDuration()
        setSampling()
        setRecordSchedule()

        binding.nextButton.setOnClickListener {
            viewModel.syncParameter(binding.scheduleChipGroup.listOfTime)
        }

        lifecycleScope.launch {
            viewModel.prefsSyncState.collectLatest {
                if (it) {
                    mainEvent?.next()
                }
            }
        }
    }

    private fun setPredefinedConfiguration(context: Context) {
        sampleRateEntries = context.resources.getStringArray(R.array.sample_rate_entries)
        sampleRateValues = context.resources.getStringArray(R.array.sample_rate_values)
        bitrateEntries = context.resources.getStringArray(R.array.bitrate_entries)
        bitrateValues = context.resources.getStringArray(R.array.bitrate_values)
        fileFormatList = context.resources.getStringArray(R.array.audio_codec)
        durationEntries = context.resources.getStringArray(R.array.duration_cycle_entries)
        durationValues = context.resources.getStringArray(R.array.duration_cycle_values)
        samplingEntries = context.resources.getStringArray(R.array.sampling_entries)
        samplingValues = context.resources.getStringArray(R.array.sampling_values)
    }

    private fun setBitrateLayout() {
        binding.bitrateValueTextView.setOnClickListener {
            val builder =
                context?.let { it1 -> MaterialAlertDialogBuilder(it1, R.style.BaseAlertDialog) }
            if (builder != null) {
                builder.setTitle("Choose Bitrate")
                    .setItems(bitrateEntries) { dialog, i ->
                        bitrateValues?.get(i)?.let {
                            viewModel.selectBitrate(it)
                        }
                    }
                val dialog = builder.create()
                dialog.show()
            }
        }
    }

    private fun setFileFormatLayout() {
        binding.fileFormatValueTextView.setOnClickListener {
            val builder =
                context?.let { it1 -> MaterialAlertDialogBuilder(it1, R.style.BaseAlertDialog) }
            if (builder != null) {
                builder.setTitle("Choose file format")
                    .setItems(fileFormatList) { dialog, i ->
                        fileFormatList?.get(i)?.let {
                            viewModel.selectFileFormat(it)
                        }
                    }
                val dialog = builder.create()
                dialog.show()
            }
        }
    }

    private fun setSampleRateLayout() {
        binding.sampleRateValueTextView.setOnClickListener {
            val builder =
                context?.let { it1 -> MaterialAlertDialogBuilder(it1, R.style.BaseAlertDialog) }
            if (builder != null) {
                builder.setTitle("Choose Sample rate")
                    .setItems(sampleRateEntries) { dialog, i ->
                        sampleRateValues?.get(i)?.let {
                            viewModel.selectSampleRate(it)
                        }
                    }
                val dialog = builder.create()
                dialog.show()
            }
        }
    }

    private fun setDuration() {
        binding.durationValueTextView.setOnClickListener {
            val builder =
                context?.let { it1 -> MaterialAlertDialogBuilder(it1, R.style.BaseAlertDialog) }
            if (builder != null) {
                builder.setTitle("Choose Duration")
                    .setItems(durationEntries) { dialog, i ->
                        durationValues?.get(i)?.let {
                            viewModel.selectDuration(it)
                        }
                    }
                val dialog = builder.create()
                dialog.show()
            }
        }
    }

    private fun setSampling() {
        binding.samplingValueTextView.setOnClickListener {
            val guidelineDialog: NumberPickerDialog =
                this.parentFragmentManager.findFragmentByTag(NumberPickerDialog::class.java.name) as NumberPickerDialog?
                    ?: run {
                        NumberPickerDialog.newInstance(if (!viewModel.enableSampling) 0 else (viewModel.sampling.toInt()), this)
                    }
            guidelineDialog.show(
                this.parentFragmentManager,
                NumberPickerDialog::class.java.name
            )
        }
    }

    private fun setRecordSchedule() {
        binding.scheduleChipGroup.fragmentManager = parentFragmentManager
        binding.scheduleChipGroup.setTimes(viewModel.schedule, true)
    }

    override fun onNextClicked(number: Int) {
        viewModel.selectSampling(number.toString())
    }

    companion object {
        fun newInstance(): GuardianAudioParameterFragment {
            return GuardianAudioParameterFragment()
        }
    }
}
