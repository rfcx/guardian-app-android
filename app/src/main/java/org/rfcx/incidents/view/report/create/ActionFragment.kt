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
		setupResponseActions()
		setOnChange()
		
		nextStepButton.setOnClickListener {
			setSelect()
		}
	}
	
	private fun setupResponseActions() {
		val response = listener.getResponse()
		response?.let { res ->
			selected.addAll(res.responseActions)
			nextStepButton.isEnabled = selected.isNotEmpty()
			setSelected()
		}
	}
	
	private fun setSelected() {
		selected.forEach { id ->
			when(id) {
				Actions.COLLECTED_EVIDENCE.value -> collectedCheckBox.isChecked = true
				Actions.ISSUE_A_WARNING.value -> warningCheckBox.isChecked = true
				Actions.CONFISCATED_EQUIPMENT.value -> confiscatedCheckBox.isChecked = true
				Actions.DAMAGED_MACHINERY.value -> damagedMachineryCheckBox.isChecked = true
			}
		}
	}
	
	private fun setOnChange() {
		collectedCheckBox.setOnClickListener {
			setEnabled()
		}
		warningCheckBox.setOnClickListener {
			setEnabled()
		}
		confiscatedCheckBox.setOnClickListener {
			setEnabled()
		}
		damagedMachineryCheckBox.setOnClickListener {
			setEnabled()
		}
	}
	
	private fun setEnabled() {
		selected.clear()
		nextStepButton.isEnabled = collectedCheckBox.isChecked ||
				warningCheckBox.isChecked || confiscatedCheckBox.isChecked ||
				damagedMachineryCheckBox.isChecked
	}
	
	private fun setSelect() {
		selected.clear()
		if (collectedCheckBox.isChecked) {
			selected.add(Actions.COLLECTED_EVIDENCE.value)
		}
		if (warningCheckBox.isChecked) {
			selected.add(Actions.ISSUE_A_WARNING.value)
		}
		if (confiscatedCheckBox.isChecked) {
			selected.add(Actions.CONFISCATED_EQUIPMENT.value)
		}
		if (damagedMachineryCheckBox.isChecked) {
			selected.add(Actions.DAMAGED_MACHINERY.value)
		}
		listener.setAction(selected)
		listener.handleCheckClicked(StepCreateReport.ASSETS.step)
	}
	
	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
	                          savedInstanceState: Bundle?): View? {
		// Inflate the layout for this fragment
		return inflater.inflate(R.layout.fragment_action, container, false)
	}
	
	companion object {
		@JvmStatic
		fun newInstance() = ActionFragment()
	}
}
