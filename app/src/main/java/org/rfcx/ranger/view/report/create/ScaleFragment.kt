package org.rfcx.ranger.view.report.create

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_scale.*
import org.rfcx.ranger.R
import org.rfcx.ranger.entity.response.LoggingScale

class ScaleFragment : Fragment() {
	
	lateinit var listener: CreateReportListener
	var selected: Int? = null
	
	override fun onAttach(context: Context) {
		super.onAttach(context)
		listener = (context as CreateReportListener)
	}
	
	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
	                          savedInstanceState: Bundle?): View? {
		// Inflate the layout for this fragment
		return inflater.inflate(R.layout.fragment_scale, container, false)
	}
	
	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		nextStepButton.setOnClickListener {
			selected?.let { value ->
				listener.setScale(value)
				listener.handleCheckClicked(StepCreateReport.DAMAGE.step)
			}
		}
		
		scaleRadioGroup.setOnCheckedChangeListener { group, checkedId ->
			nextStepButton.isEnabled = true
			
			when (checkedId) {
				R.id.notSureRadioButton -> selected = LoggingScale.NOT_SURE.value
				R.id.smallRadioButton -> selected = LoggingScale.SMALL.value
				R.id.largeRadioButton -> selected = LoggingScale.LARGE.value
			}
		}
	}
	
	companion object {
		@JvmStatic
		fun newInstance() = ScaleFragment()
	}
}
