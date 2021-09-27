package org.rfcx.ranger.view.report.create

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_scale.*
import org.rfcx.ranger.R

class ScaleFragment : Fragment() {
	
	lateinit var listener: CreateReportListener
	
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
			listener.handleCheckClicked(StepCreateReport.DAMAGE.step)
		}
	}
	
	companion object {
		@JvmStatic
		fun newInstance() = ScaleFragment()
	}
}
