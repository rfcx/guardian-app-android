package org.rfcx.ranger.view.report.create

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_action.*
import org.rfcx.ranger.R
import org.rfcx.ranger.entity.response.EvidenceTypes
import java.util.*

class ActionFragment : Fragment() {
	
	lateinit var listener: CreateReportListener
	private var selected = ArrayList<Int>()
	
	override fun onAttach(context: Context) {
		super.onAttach(context)
		listener = (context as CreateReportListener)
	}
	
	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		setOnChange()
		
		nextStepButton.setOnClickListener {
			setSelect()
		}
	}
	
	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
	                          savedInstanceState: Bundle?): View? {
		// Inflate the layout for this fragment
		return inflater.inflate(R.layout.fragment_action, container, false)
	}
	
	private fun setOnChange() {
		collectedEvidenceCheckBox.setOnClickListener {
			setEnabled()
		}
		issueWarningCheckBox.setOnClickListener {
			setEnabled()
		}
		confiscatedEquipmentCheckBox.setOnClickListener {
			setEnabled()
		}
		issueFineCheckBox.setOnClickListener {
			setEnabled()
		}
		arrestsCheckBox.setOnClickListener {
			setEnabled()
		}
		planningSecurityCheckBox.setOnClickListener {
			setEnabled()
		}
		otherCheckBox.setOnClickListener {
			setEnabled()
		}
		noneCheckBox.setOnClickListener {
			setEnabled()
		}
	}
	
	private fun setEnabled() {
		nextStepButton.isEnabled = collectedEvidenceCheckBox.isChecked ||
				issueWarningCheckBox.isChecked || confiscatedEquipmentCheckBox.isChecked ||
				issueFineCheckBox.isChecked || arrestsCheckBox.isChecked ||
				planningSecurityCheckBox.isChecked || otherCheckBox.isChecked ||
				noneCheckBox.isChecked
	}
	
	private fun setSelect() {
		if (collectedEvidenceCheckBox.isChecked) {
			selected.add(EvidenceTypes.CUT_DOWN_TREES.value)
		}
		if (issueWarningCheckBox.isChecked) {
			selected.add(EvidenceTypes.CLEARED_AREAS.value)
		}
		if (confiscatedEquipmentCheckBox.isChecked) {
			selected.add(EvidenceTypes.LOGGING_EQUIPMENT.value)
		}
		if (issueFineCheckBox.isChecked) {
			selected.add(EvidenceTypes.LOGGERS_AT_SITE.value)
		}
		if (arrestsCheckBox.isChecked) {
			selected.add(EvidenceTypes.ILLEGAL_CAMPS.value)
		}
		if (planningSecurityCheckBox.isChecked) {
			selected.add(EvidenceTypes.FIRED_BURNED_AREAS.value)
		}
		if (otherCheckBox.isChecked) {
			selected.add(EvidenceTypes.EVIDENCE_OF_POACHING.value)
		}
		if (noneCheckBox.isChecked) {
			selected.add(EvidenceTypes.NONE.value)
		}
		
		listener.setAction(selected)
		listener.handleCheckClicked(StepCreateReport.ASSETS.step)
	}
	
	companion object {
		@JvmStatic
		fun newInstance() = ActionFragment()
	}
}
