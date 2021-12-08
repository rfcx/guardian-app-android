package org.rfcx.incidents.view.report.create

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_scale.*
import org.rfcx.incidents.R
import org.rfcx.incidents.entity.response.PoachingScale

class PoachingScaleFragment : Fragment() {
	
	lateinit var listener: CreateReportListener
	var selected: Int? = null
	
	override fun onAttach(context: Context) {
		super.onAttach(context)
		listener = (context as CreateReportListener)
	}
	
	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
	                          savedInstanceState: Bundle?): View? {
		// Inflate the layout for this fragment
		return inflater.inflate(R.layout.fragment_poaching_scale, container, false)
	}
	
	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		siteNameTextView.text = getString(R.string.site_name, listener.getSiteName())
		
		nextStepButton.setOnClickListener {
			selected?.let { value ->
				listener.handleCheckClicked(StepCreateReport.ACTION.step)
			}
		}
		
		scaleRadioGroup.setOnCheckedChangeListener { group, checkedId ->
			nextStepButton.isEnabled = true
			
			when (checkedId) {
				R.id.smallRadioButton -> selected = PoachingScale.SMALL.value
				R.id.largeRadioButton -> selected = PoachingScale.LARGE.value
				R.id.noneRadioButton -> selected = PoachingScale.NONE.value
			}
		}
	}
	
	companion object {
		@JvmStatic
		fun newInstance() = PoachingScaleFragment()
	}
}
