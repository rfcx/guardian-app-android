package org.rfcx.incidents.view.report.create

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import org.rfcx.incidents.databinding.FragmentEvidenceBinding
import org.rfcx.incidents.entity.response.EvidenceTypes
import org.rfcx.incidents.util.Analytics
import org.rfcx.incidents.util.Screen

class EvidenceFragment : Fragment() {
    private var _binding: FragmentEvidenceBinding? = null
    private val binding get() = _binding!!

    companion object {
        @JvmStatic
        fun newInstance() = EvidenceFragment()
    }

    private val analytics by lazy { context?.let { Analytics(it) } }
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
        _binding = FragmentEvidenceBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setOnChange()
        setupEvidences()

        binding.nextStepButton.setOnClickListener {
            setSelect()
        }
    }

    override fun onResume() {
        super.onResume()
        analytics?.trackScreen(Screen.EVIDENCE)
    }

    private fun setupEvidences() {
        val response = listener.getResponse()
        response?.let { res ->
            selected.addAll(res.evidences)
            binding.nextStepButton.isEnabled = selected.isNotEmpty()
            setSelected()
        }
    }

    private fun setOnChange() {
        binding.cutDownTreesCheckBox.setOnClickListener {
            setSelectedNone(false)
            setEnabled()
        }
        binding.clearedAreasCheckBox.setOnClickListener {
            setSelectedNone(false)
            setEnabled()
        }
        binding.loggingEquipmentCheckBox.setOnClickListener {
            setSelectedNone(false)
            setEnabled()
        }
        binding.loggersAtSiteCheckBox.setOnClickListener {
            setSelectedNone(false)
            setEnabled()
        }
        binding.illegalCampsCheckBox.setOnClickListener {
            setSelectedNone(false)
            setEnabled()
        }
        binding.firesBurnedAreasCheckBox.setOnClickListener {
            setSelectedNone(false)
            setEnabled()
        }
        binding.otherCheckBox.setOnClickListener {
            setSelectedNone(false)
            setEnabled()
        }
        binding.noneCheckBox.setOnClickListener {
            setSelectedNone(true)
            setEnabled()
        }
    }

    private fun setSelectedNone(isNone: Boolean) {
        if (isNone) {
            binding.cutDownTreesCheckBox.isChecked = false
            binding.clearedAreasCheckBox.isChecked = false
            binding.loggingEquipmentCheckBox.isChecked = false
            binding.loggersAtSiteCheckBox.isChecked = false
            binding.illegalCampsCheckBox.isChecked = false
            binding.firesBurnedAreasCheckBox.isChecked = false
            binding.otherCheckBox.isChecked = false
        }
        binding.noneCheckBox.isChecked = isNone
    }

    private fun setEnabled() {
        selected.clear()
        binding.nextStepButton.isEnabled = binding.cutDownTreesCheckBox.isChecked ||
            binding.clearedAreasCheckBox.isChecked || binding.loggingEquipmentCheckBox.isChecked ||
            binding.loggersAtSiteCheckBox.isChecked || binding.illegalCampsCheckBox.isChecked ||
            binding.firesBurnedAreasCheckBox.isChecked || binding.otherCheckBox.isChecked || binding.noneCheckBox.isChecked
    }

    private fun setSelect() {
        selected.clear()
        if (binding.cutDownTreesCheckBox.isChecked) {
            selected.add(EvidenceTypes.CUT_DOWN_TREES.value)
        }
        if (binding.clearedAreasCheckBox.isChecked) {
            selected.add(EvidenceTypes.CLEARED_AREAS.value)
        }
        if (binding.loggingEquipmentCheckBox.isChecked) {
            selected.add(EvidenceTypes.LOGGING_EQUIPMENT.value)
        }
        if (binding.loggersAtSiteCheckBox.isChecked) {
            selected.add(EvidenceTypes.LOGGERS_AT_SITE.value)
        }
        if (binding.illegalCampsCheckBox.isChecked) {
            selected.add(EvidenceTypes.ILLEGAL_CAMPS.value)
        }
        if (binding.firesBurnedAreasCheckBox.isChecked) {
            selected.add(EvidenceTypes.FIRED_BURNED_AREAS.value)
        }
        if (binding.otherCheckBox.isChecked) {
            selected.add(EvidenceTypes.OTHER.value)
        }
        if (binding.noneCheckBox.isChecked) {
            selected.add(EvidenceTypes.NONE.value)
        }

        listener.setEvidence(selected)
        listener.handleCheckClicked(StepCreateReport.SCALE.step)
    }

    private fun setSelected() {
        selected.forEach { id ->
            when (id) {
                EvidenceTypes.CUT_DOWN_TREES.value -> binding.cutDownTreesCheckBox.isChecked = true
                EvidenceTypes.CLEARED_AREAS.value -> binding.clearedAreasCheckBox.isChecked = true
                EvidenceTypes.LOGGING_EQUIPMENT.value -> binding.loggingEquipmentCheckBox.isChecked = true
                EvidenceTypes.LOGGERS_AT_SITE.value -> binding.loggersAtSiteCheckBox.isChecked = true
                EvidenceTypes.ILLEGAL_CAMPS.value -> binding.illegalCampsCheckBox.isChecked = true
                EvidenceTypes.FIRED_BURNED_AREAS.value -> binding.firesBurnedAreasCheckBox.isChecked = true
                EvidenceTypes.OTHER.value -> binding.otherCheckBox.isChecked = true
                EvidenceTypes.NONE.value -> binding.noneCheckBox.isChecked = true
            }
        }
    }
}
