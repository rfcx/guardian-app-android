package org.rfcx.incidents.view.report.create

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import org.rfcx.incidents.databinding.FragmentPoachingEvidenceBinding
import org.rfcx.incidents.entity.response.PoachingEvidence

class PoachingEvidenceFragment : Fragment() {
    private var _binding: FragmentPoachingEvidenceBinding? = null
    private val binding get() = _binding!!
    lateinit var listener: CreateReportListener
    private var selected = ArrayList<Int>()

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = (context as CreateReportListener)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPoachingEvidenceBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setOnChange()
        setupPoachingEvidence()

        binding.nextStepButton.setOnClickListener {
            setSelect()
        }
    }

    private fun setupPoachingEvidence() {
        val response = listener.getResponse()
        response?.let { res ->
            selected.addAll(res.poachingEvidence)
            binding.nextStepButton.isEnabled = selected.isNotEmpty()
            setSelected()
        }
    }

    private fun setOnChange() {
        binding.bulletShellsCheckBox.setOnClickListener {
            setSelectedNone(false)
            setEnabled()
        }
        binding.footprintsCheckBox.setOnClickListener {
            setSelectedNone(false)
            setEnabled()
        }
        binding.dogTracksCheckBox.setOnClickListener {
            setSelectedNone(false)
            setEnabled()
        }
        binding.otherSpecifyCheckBox.setOnClickListener {
            setSelectedNone(false)
            setEnabled()
        }
        binding.noneCheckBox.setOnClickListener {
            setSelectedNone(true)
            setEnabled()
        }
    }

    private fun setEnabled() {
        selected.clear()
        binding.nextStepButton.isEnabled = binding.bulletShellsCheckBox.isChecked ||
            binding.footprintsCheckBox.isChecked || binding.dogTracksCheckBox.isChecked ||
            binding.otherSpecifyCheckBox.isChecked || binding.noneCheckBox.isChecked
    }

    private fun setSelect() {
        selected.clear()
        if (binding.bulletShellsCheckBox.isChecked) {
            selected.add(PoachingEvidence.BULLET_SHELLS.value)
        }
        if (binding.footprintsCheckBox.isChecked) {
            selected.add(PoachingEvidence.FOOTPRINTS.value)
        }
        if (binding.dogTracksCheckBox.isChecked) {
            selected.add(PoachingEvidence.DOG_TRACKS.value)
        }
        if (binding.otherSpecifyCheckBox.isChecked) {
            selected.add(PoachingEvidence.OTHER.value)
        }
        if (binding.noneCheckBox.isChecked) {
            selected.add(PoachingEvidence.NONE.value)
        }
        listener.setPoachingEvidence(selected)
        listener.handleCheckClicked(StepCreateReport.SCALE_POACHING.step)
    }

    private fun setSelectedNone(isNone: Boolean) {
        if (isNone) {
            binding.bulletShellsCheckBox.isChecked = false
            binding.footprintsCheckBox.isChecked = false
            binding.dogTracksCheckBox.isChecked = false
            binding.otherSpecifyCheckBox.isChecked = false
            binding.otherSpecifyCheckBox.isChecked = false
        }
        binding.noneCheckBox.isChecked = isNone
    }

    private fun setSelected() {
        selected.forEach { id ->
            when (id) {
                PoachingEvidence.BULLET_SHELLS.value -> binding.bulletShellsCheckBox.isChecked = true
                PoachingEvidence.FOOTPRINTS.value -> binding.footprintsCheckBox.isChecked = true
                PoachingEvidence.DOG_TRACKS.value -> binding.dogTracksCheckBox.isChecked = true
                PoachingEvidence.OTHER.value -> binding.otherSpecifyCheckBox.isChecked = true
                PoachingEvidence.NONE.value -> binding.noneCheckBox.isChecked = true
            }
        }
    }

    companion object {
        @JvmStatic
        fun newInstance() = PoachingEvidenceFragment()
    }
}
