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
import java.util.*

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
	
	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
	                          savedInstanceState: Bundle?): View? {
		// Inflate the layout for this fragment
		return inflater.inflate(R.layout.fragment_evidence, container, false)
	}
	
	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		setOnChange()
		setupEvidences()
		siteNameTextView.text = getString(R.string.site_name, listener.getSiteName())
		
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
			setEnabled()
		}
		clearedAreasCheckBox.setOnClickListener {
			setEnabled()
		}
		loggingEquipmentCheckBox.setOnClickListener {
			setEnabled()
		}
		loggersAtSiteCheckBox.setOnClickListener {
			setEnabled()
		}
		illegalCampsCheckBox.setOnClickListener {
			setEnabled()
		}
		firesBurnedAreasCheckBox.setOnClickListener {
			setEnabled()
		}
	}
	
	private fun setEnabled() {
		selected.clear()
		nextStepButton.isEnabled = cutDownTreesCheckBox.isChecked ||
				clearedAreasCheckBox.isChecked || loggingEquipmentCheckBox.isChecked ||
				loggersAtSiteCheckBox.isChecked || illegalCampsCheckBox.isChecked ||
				firesBurnedAreasCheckBox.isChecked
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
		
		listener.setEvidence(selected)
		listener.handleCheckClicked(StepCreateReport.SCALE.step)
	}
	
	private fun setSelected() {
		selected.forEach { id ->
			if (id == EvidenceTypes.CUT_DOWN_TREES.value) {
				cutDownTreesCheckBox.isChecked = true
			}
			if (id == EvidenceTypes.CLEARED_AREAS.value) {
				clearedAreasCheckBox.isChecked = true
			}
			if (id == EvidenceTypes.LOGGING_EQUIPMENT.value) {
				loggingEquipmentCheckBox.isChecked = true
			}
			if (id == EvidenceTypes.LOGGERS_AT_SITE.value) {
				loggersAtSiteCheckBox.isChecked = true
			}
			if (id == EvidenceTypes.ILLEGAL_CAMPS.value) {
				illegalCampsCheckBox.isChecked = true
			}
			if (id == EvidenceTypes.FIRED_BURNED_AREAS.value) {
				firesBurnedAreasCheckBox.isChecked = true
			}
		}
	}
}
