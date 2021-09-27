package org.rfcx.ranger.view.report.draft

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_draft_reports.*
import org.rfcx.ranger.R
import org.rfcx.ranger.entity.response.Response

class DraftReportsFragment : Fragment(), ReportOnClickListener {
	
	private val reportsAdapter by lazy { DraftReportsAdapter(this) }
	
	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
	                          savedInstanceState: Bundle?): View? {
		// Inflate the layout for this fragment
		return inflater.inflate(R.layout.fragment_draft_reports, container, false)
	}
	
	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		
		draftReportsRecyclerView.apply {
			layoutManager = LinearLayoutManager(context)
			adapter = reportsAdapter
		}
		reportsAdapter.items = listOf() // Add list of ReportModel and should sortedByDescending( date )
	}
	
	companion object {
		const val tag = "DraftReportsFragment"
		
		@JvmStatic
		fun newInstance() = DraftReportsFragment()
	}
	
	override fun onClickedDelete(response: Response) {
		Toast.makeText(context, "On click delete ${response.guardianName}", Toast.LENGTH_SHORT).show()
	}
}
