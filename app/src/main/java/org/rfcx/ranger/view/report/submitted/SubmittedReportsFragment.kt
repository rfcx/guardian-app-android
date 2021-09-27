package org.rfcx.ranger.view.report.submitted

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_submitted_reports.*
import org.rfcx.ranger.R

class SubmittedReportsFragment : Fragment() {
	private val reportsAdapter by lazy { SubmittedReportsAdapter() }
	
	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
	                          savedInstanceState: Bundle?): View? {
		// Inflate the layout for this fragment
		return inflater.inflate(R.layout.fragment_submitted_reports, container, false)
	}
	
	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		submittedReportsRecyclerView.apply {
			layoutManager = LinearLayoutManager(context)
			adapter = reportsAdapter
		}
		reportsAdapter.items = listOf() // Add list of ReportModel and should sortedByDescending( date )
	}
	
	companion object {
		const val tag = "SubmittedReportsFragment"
		
		@JvmStatic
		fun newInstance() = SubmittedReportsFragment()
	}
}
