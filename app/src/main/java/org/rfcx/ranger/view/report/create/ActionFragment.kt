package org.rfcx.ranger.view.report.create

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_action.*
import org.rfcx.ranger.R
import org.rfcx.ranger.entity.response.Actions
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
		setupAction()
		
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
		selected.clear()
		nextStepButton.isEnabled = collectedEvidenceCheckBox.isChecked ||
				issueWarningCheckBox.isChecked || confiscatedEquipmentCheckBox.isChecked ||
				issueFineCheckBox.isChecked || arrestsCheckBox.isChecked ||
				planningSecurityCheckBox.isChecked || otherCheckBox.isChecked ||
				noneCheckBox.isChecked
	}
	
	private fun setSelect() {
		selected.clear()
		if (collectedEvidenceCheckBox.isChecked) {
			selected.add(Actions.COLLECTED_EVIDENCE.value)
		}
		if (issueWarningCheckBox.isChecked) {
			selected.add(Actions.ISSUE_A_WARNING.value)
		}
		if (confiscatedEquipmentCheckBox.isChecked) {
			selected.add(Actions.CONFISCATED_EQUIPMENT.value)
		}
		if (issueFineCheckBox.isChecked) {
			selected.add(Actions.ISSUE_A_FINE.value)
		}
		if (arrestsCheckBox.isChecked) {
			selected.add(Actions.ARRESTS.value)
		}
		if (planningSecurityCheckBox.isChecked) {
			selected.add(Actions.PLANNING_TO_COME_BACK_WITH_SECURITY_ENFORCEMENT.value)
		}
		if (otherCheckBox.isChecked) {
			selected.add(Actions.OTHER.value)
		}
		if (noneCheckBox.isChecked) {
			selected.add(Actions.NONE.value)
		}
		
		listener.setAction(selected)
		listener.handleCheckClicked(StepCreateReport.ASSETS.step)
	}
	
	private fun setupAction() {
		val actions = listener.getResponse()?.responseActions?.toList()
		actions?.let {
			selected.addAll(it)
			nextStepButton.isEnabled = selected.isNotEmpty()
			if (actions.isNotEmpty()) setupCheckBox()
		}
	}
	
	private fun setupCheckBox() {
		selected.forEach { value ->
			when (value) {
				Actions.COLLECTED_EVIDENCE.value -> collectedEvidenceCheckBox.isChecked = true
				Actions.ISSUE_A_WARNING.value -> issueWarningCheckBox.isChecked = true
				Actions.CONFISCATED_EQUIPMENT.value -> confiscatedEquipmentCheckBox.isChecked = true
				Actions.ISSUE_A_FINE.value -> issueFineCheckBox.isChecked = true
				Actions.ARRESTS.value -> arrestsCheckBox.isChecked = true
				Actions.PLANNING_TO_COME_BACK_WITH_SECURITY_ENFORCEMENT.value -> planningSecurityCheckBox.isChecked = true
				Actions.OTHER.value -> otherCheckBox.isChecked = true
				Actions.NONE.value -> noneCheckBox.isChecked = true
			}
		}
	}
	
	companion object {
		@JvmStatic
		fun newInstance() = ActionFragment()
	}
}
