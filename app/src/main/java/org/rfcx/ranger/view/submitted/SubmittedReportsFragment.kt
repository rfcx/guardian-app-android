package org.rfcx.ranger.view.submitted

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import org.rfcx.ranger.R

class SubmittedReportsFragment : Fragment() {
	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
	                          savedInstanceState: Bundle?): View? {
		// Inflate the layout for this fragment
		return inflater.inflate(R.layout.fragment_submitted_reports, container, false)
	}
	
	companion object {
		const val tag = "SubmittedReportsFragment"
		
		@JvmStatic
		fun newInstance() = SubmittedReportsFragment()
	}
}
