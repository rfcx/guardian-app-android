package org.rfcx.incidents.view.report.create

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_evidence.*
import org.rfcx.incidents.R
import org.rfcx.incidents.entity.response.EvidenceTypes
import org.rfcx.incidents.util.Analytics
import org.rfcx.incidents.util.Screen

class EvidenceFragment : Fragment() {
    
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
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_evidence, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setOnChange()
        setupEvidences()
        
        nextStepButton.setOnClickListener {
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
            nextStepButton.isEnabled = selected.isNotEmpty()
            setSelected()
        }
    }
    
    private fun setOnChange() {
        cutDownTreesCheckBox.setOnClickListener {
            setSelectedNone(false)
            setEnabled()
        }
        clearedAreasCheckBox.setOnClickListener {
            setSelectedNone(false)
            setEnabled()
        }
        loggingEquipmentCheckBox.setOnClickListener {
            setSelectedNone(false)
            setEnabled()
        }
        loggersAtSiteCheckBox.setOnClickListener {
            setSelectedNone(false)
            setEnabled()
        }
        illegalCampsCheckBox.setOnClickListener {
            setSelectedNone(false)
            setEnabled()
        }
        firesBurnedAreasCheckBox.setOnClickListener {
            setSelectedNone(false)
            setEnabled()
        }
        otherCheckBox.setOnClickListener {
            setSelectedNone(false)
            setEnabled()
        }
        noneCheckBox.setOnClickListener {
            setSelectedNone(true)
            setEnabled()
        }
    }
    
    private fun setSelectedNone(isNone: Boolean) {
        if (isNone) {
            cutDownTreesCheckBox.isChecked = false
            clearedAreasCheckBox.isChecked = false
            loggingEquipmentCheckBox.isChecked = false
            loggersAtSiteCheckBox.isChecked = false
            illegalCampsCheckBox.isChecked = false
            firesBurnedAreasCheckBox.isChecked = false
            otherCheckBox.isChecked = false
        }
        noneCheckBox.isChecked = isNone
    }
    
    
    private fun setEnabled() {
        selected.clear()
        nextStepButton.isEnabled = cutDownTreesCheckBox.isChecked ||
                clearedAreasCheckBox.isChecked || loggingEquipmentCheckBox.isChecked ||
                loggersAtSiteCheckBox.isChecked || illegalCampsCheckBox.isChecked ||
                firesBurnedAreasCheckBox.isChecked || otherCheckBox.isChecked || noneCheckBox.isChecked
    }
    
    private fun setSelect() {
        selected.clear()
        if (cutDownTreesCheckBox.isChecked) {
            selected.add(EvidenceTypes.CUT_DOWN_TREES.value)
        }
        if (clearedAreasCheckBox.isChecked) {
            selected.add(EvidenceTypes.CLEARED_AREAS.value)
        }
        if (loggingEquipmentCheckBox.isChecked) {
            selected.add(EvidenceTypes.LOGGING_EQUIPMENT.value)
        }
        if (loggersAtSiteCheckBox.isChecked) {
            selected.add(EvidenceTypes.LOGGERS_AT_SITE.value)
        }
        if (illegalCampsCheckBox.isChecked) {
            selected.add(EvidenceTypes.ILLEGAL_CAMPS.value)
        }
        if (firesBurnedAreasCheckBox.isChecked) {
            selected.add(EvidenceTypes.FIRED_BURNED_AREAS.value)
        }
        if (otherCheckBox.isChecked) {
            selected.add(EvidenceTypes.OTHER.value)
        }
        if (noneCheckBox.isChecked) {
            selected.add(EvidenceTypes.NONE.value)
        }
        
        listener.setEvidence(selected)
        listener.handleCheckClicked(StepCreateReport.SCALE.step)
    }
    
    private fun setSelected() {
        selected.forEach { id ->
            when (id) {
                EvidenceTypes.CUT_DOWN_TREES.value -> cutDownTreesCheckBox.isChecked = true
                EvidenceTypes.CLEARED_AREAS.value -> clearedAreasCheckBox.isChecked = true
                EvidenceTypes.LOGGING_EQUIPMENT.value -> loggingEquipmentCheckBox.isChecked = true
                EvidenceTypes.LOGGERS_AT_SITE.value -> loggersAtSiteCheckBox.isChecked = true
                EvidenceTypes.ILLEGAL_CAMPS.value -> illegalCampsCheckBox.isChecked = true
                EvidenceTypes.FIRED_BURNED_AREAS.value -> firesBurnedAreasCheckBox.isChecked = true
                EvidenceTypes.OTHER.value -> otherCheckBox.isChecked = true
                EvidenceTypes.NONE.value -> noneCheckBox.isChecked = true
            }
        }
    }
}
