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

class InvestigationTypeFragment : Fragment() {
	private val analytics by lazy { context?.let { Analytics(it) } }
	lateinit var listener: CreateReportListener
	var selected: Int? = null
	
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
		siteNameTextView.text = getString(R.string.site_name, listener.getSiteName())
		
		nextStepButton.setOnClickListener {
			when (selected) {
				InvestigationType.LOGGING.value -> listener.handleCheckClicked(StepCreateReport.EVIDENCE.step)
				InvestigationType.POACHING.value -> listener.handleCheckClicked(StepCreateReport.EVIDENCE.step) // TODO:: handle
				InvestigationType.OTHER.value -> listener.handleCheckClicked(StepCreateReport.ASSETS.step)
			}
		}
		
		typeRadioGroup.setOnCheckedChangeListener { group, checkedId ->
			nextStepButton.isEnabled = true
			
			when (checkedId) {
				R.id.loggingRadioButton -> selected = InvestigationType.LOGGING.value
				R.id.poachingRadioButton -> selected = InvestigationType.POACHING.value
				R.id.otherRadioButton -> selected = InvestigationType.OTHER.value
			}
		}
	}
	
	companion object {
		@JvmStatic
		fun newInstance() = InvestigationTypeFragment()
	}
}
