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
		setupScale()
		
		nextStepButton.setOnClickListener {
			selected?.let { value ->
				listener.setScale(value)
				listener.handleCheckClicked(StepCreateReport.DAMAGE.step)
			}
		}
		
		scaleRadioGroup.setOnCheckedChangeListener { group, checkedId ->
			nextStepButton.isEnabled = true
			
			when (checkedId) {
				R.id.smallRadioButton -> selected = LoggingScale.SMALL.value
				R.id.largeRadioButton -> selected = LoggingScale.LARGE.value
				R.id.noneRadioButton -> selected = LoggingScale.NONE.value
			}
		}
	}
	
	private fun setupScale() {
		val response = listener.getResponse()
		response?.let { res ->
			selected = res.loggingScale
			nextStepButton.isEnabled = selected != LoggingScale.DEFAULT.value
			when (selected) {
				LoggingScale.SMALL.value -> smallRadioButton.isChecked = true
				LoggingScale.LARGE.value -> largeRadioButton.isChecked = true
				LoggingScale.NONE.value -> noneRadioButton.isChecked = true
			}
		}
	}
	
	companion object {
		@JvmStatic
		fun newInstance() = ScaleFragment()
	}
}
