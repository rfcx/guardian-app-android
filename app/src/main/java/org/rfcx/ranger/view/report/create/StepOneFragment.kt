package org.rfcx.ranger.view.report.create

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import org.rfcx.ranger.R

class StepOneFragment : Fragment() {
	
	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
	                          savedInstanceState: Bundle?): View? {
		// Inflate the layout for this fragment
		return inflater.inflate(R.layout.fragment_step_one, container, false)
	}
	
	companion object {
		@JvmStatic
		fun newInstance() = StepOneFragment()
	}
}
