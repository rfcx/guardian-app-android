package org.rfcx.incidents.view.report.create

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_action.*
import org.rfcx.incidents.R
import org.rfcx.incidents.entity.response.Actions
import org.rfcx.incidents.util.Analytics
import org.rfcx.incidents.util.Screen
import java.util.*

class ActionFragment : Fragment() {
	
	private val analytics by lazy { context?.let { Analytics(it) } }
	lateinit var listener: CreateReportListener
	private var selected = ArrayList<Int>()
	
	override fun onAttach(context: Context) {
		super.onAttach(context)
		listener = (context as CreateReportListener)
	}
	
	override fun onResume() {
		super.onResume()
		analytics?.trackScreen(Screen.ACTION)
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
			setWhenSelectedOtherAndNone()
			setEnabled()
		}
		issueWarningCheckBox.setOnClickListener {
			setWhenSelectedOtherAndNone()
			setEnabled()
		}
		confiscatedEquipmentCheckBox.setOnClickListener {
			setWhenSelectedOtherAndNone()
			setEnabled()
		}
		arrestsCheckBox.setOnClickListener {
			setWhenSelectedOtherAndNone()
			setEnabled()
		}
		planningSecurityCheckBox.setOnClickListener {
			setWhenSelectedOtherAndNone()
			setEnabled()
		}
		otherCheckBox.setOnClickListener {
			setWhenSelectedOtherAndNone(selectedOther = true)
			setEnabled()
		}
		noneCheckBox.setOnClickListener {
			setWhenSelectedOtherAndNone(selectedNone = true)
			setEnabled()
		}
	}
	
	private fun setEnabled() {
		selected.clear()
		nextStepButton.isEnabled = collectedEvidenceCheckBox.isChecked ||
				issueWarningCheckBox.isChecked || confiscatedEquipmentCheckBox.isChecked ||
				arrestsCheckBox.isChecked || planningSecurityCheckBox.isChecked ||
				otherCheckBox.isChecked || noneCheckBox.isChecked
	}
	
	private fun setWhenSelectedOtherAndNone(selectedOther: Boolean = false, selectedNone: Boolean = false) {
		if (selectedOther || selectedNone) {
			collectedEvidenceCheckBox.isChecked = false
			issueWarningCheckBox.isChecked = false
			confiscatedEquipmentCheckBox.isChecked = false
			arrestsCheckBox.isChecked = false
			planningSecurityCheckBox.isChecked = false
		}
		otherCheckBox.isChecked = selectedOther
		noneCheckBox.isChecked = selectedNone
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
