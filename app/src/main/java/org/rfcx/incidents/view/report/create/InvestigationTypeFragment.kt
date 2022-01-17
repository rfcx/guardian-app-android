package org.rfcx.incidents.view.report.create

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_investigation_type.*
import org.rfcx.incidents.R
import org.rfcx.incidents.entity.response.InvestigationType
import org.rfcx.incidents.util.Analytics
import org.rfcx.incidents.util.Screen
import java.util.*

class InvestigationTypeFragment : Fragment() {
	private val analytics by lazy { context?.let { Analytics(it) } }
	lateinit var listener: CreateReportListener
	private var selected = ArrayList<Int>()
	
	override fun onAttach(context: Context) {
		super.onAttach(context)
		listener = (context as CreateReportListener)
	}
	
	override fun onResume() {
		super.onResume()
		analytics?.trackScreen(Screen.INVESTIGATION_TYPE)
	}
	
	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
	                          savedInstanceState: Bundle?): View? {
		return inflater.inflate(R.layout.fragment_investigation_type, container, false)
	}
	
	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		setOnChange()
		setupInvestigateType()
		
		nextStepButton.setOnClickListener {
			selected.clear()
			if (loggingCheckBox.isChecked) {
				selected.add(InvestigationType.LOGGING.value)
			}
			if (poachingCheckBox.isChecked) {
				selected.add(InvestigationType.POACHING.value)
			}
			if (otherCheckBox.isChecked) {
				selected.add(InvestigationType.OTHER.value)
			}
			listener.setInvestigateType(selected)
			
			when {
				selected.contains(InvestigationType.LOGGING.value) -> {
					listener.handleCheckClicked(StepCreateReport.EVIDENCE.step)
				}
				selected.contains(InvestigationType.POACHING.value) -> {
					listener.handleCheckClicked(StepCreateReport.POACHING_EVIDENCE.step)
				}
				selected.contains(InvestigationType.OTHER.value) -> {
					listener.handleCheckClicked(StepCreateReport.ASSETS.step)
				}
			}
		}
	}
	
	private fun setupInvestigateType() {
		val response = listener.getResponse()
		response?.let { res ->
			selected.addAll(res.investigateType)
			nextStepButton.isEnabled = selected.isNotEmpty()
			setSelected()
		}
	}
	
	private fun setSelected() {
		selected.forEach { id ->
			when(id) {
				InvestigationType.LOGGING.value -> loggingCheckBox.isChecked = true
				InvestigationType.POACHING.value -> poachingCheckBox.isChecked = true
				InvestigationType.OTHER.value -> otherCheckBox.isChecked = true
			}
		}
	}
	
	private fun setOnChange() {
		loggingCheckBox.setOnClickListener {
			setEnabled()
		}
		poachingCheckBox.setOnClickListener {
			setEnabled()
		}
		otherCheckBox.setOnClickListener {
			setEnabled()
		}
	}
	
	private fun setEnabled() {
		selected.clear()
		nextStepButton.isEnabled = loggingCheckBox.isChecked ||
				poachingCheckBox.isChecked || otherCheckBox.isChecked
	}
	
	companion object {
		@JvmStatic
		fun newInstance() = InvestigationTypeFragment()
	}
}
