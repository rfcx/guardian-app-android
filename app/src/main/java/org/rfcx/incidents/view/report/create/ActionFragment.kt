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

class ActionFragment : Fragment() {
	
	private val analytics by lazy { context?.let { Analytics(it) } }
	lateinit var listener: CreateReportListener
	var selected: Int? = null
	
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
		
		nextStepButton.setOnClickListener {
			selected?.let { listener.setAction(listOf(it)) }
			listener.handleCheckClicked(StepCreateReport.ASSETS.step)
		}
		
		actionRadioGroup.setOnCheckedChangeListener { group, checkedId ->
			nextStepButton.isEnabled = true
			
			when (checkedId) {
				R.id.collectedRadioButton -> selected = Actions.COLLECTED_EVIDENCE.value
				R.id.warningRadioButton -> selected = Actions.ISSUE_A_WARNING.value
				R.id.confiscatedRadioButton -> selected = Actions.CONFISCATED_EQUIPMENT.value
				R.id.damagedMachineryRadioButton -> selected = Actions.DAMAGED_MACHINERY.value
			}
		}
	}
	
	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
	                          savedInstanceState: Bundle?): View? {
		// Inflate the layout for this fragment
		return inflater.inflate(R.layout.fragment_action, container, false)
	}
	
	private fun setupResponseActions() {
		val response = listener.getResponse()
		response?.let { res ->
			val list = res.responseActions
			nextStepButton.isEnabled = list.isNotEmpty()
			if (list.isNotEmpty()) {
				when (list[0]) {
					Actions.COLLECTED_EVIDENCE.value -> collectedRadioButton.isChecked = true
					Actions.ISSUE_A_WARNING.value -> warningRadioButton.isChecked = true
					Actions.CONFISCATED_EQUIPMENT.value -> confiscatedRadioButton.isChecked = true
					Actions.DAMAGED_MACHINERY.value -> damagedMachineryRadioButton.isChecked = true
				}
			}
		}
	}
	
	companion object {
		@JvmStatic
		fun newInstance() = ActionFragment()
	}
}
