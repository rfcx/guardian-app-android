package org.rfcx.incidents.view.report.draft

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_draft_reports.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.rfcx.incidents.R
import org.rfcx.incidents.entity.response.Response
import org.rfcx.incidents.entity.response.SyncState
import org.rfcx.incidents.view.MainActivityEventListener
import org.rfcx.incidents.view.MainActivityViewModel

class DraftReportsFragment : Fragment(), ReportOnClickListener {
	private val viewModel: MainActivityViewModel by viewModel()
	private val reportsAdapter by lazy { DraftReportsAdapter(this) }
	lateinit var listener: MainActivityEventListener
	
	override fun onAttach(context: Context) {
		super.onAttach(context)
		listener = (context as MainActivityEventListener)
	}
	
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
		setObserve()
	}
	
	private fun setObserve() {
		viewModel.getResponses().observe(viewLifecycleOwner, { responses ->
			notHaveDraftReportsGroupView.visibility = if (responses.isEmpty()) View.VISIBLE else View.GONE
			reportsAdapter.items = responses.sortedByDescending { r -> r.investigatedAt }
		})
	}
	
	companion object {
		const val tag = "DraftReportsFragment"
		
		@JvmStatic
		fun newInstance() = DraftReportsFragment()
	}
	
	override fun onClickedDelete(response: Response) {
		Toast.makeText(context, "On click delete ${response.streamName}", Toast.LENGTH_SHORT).show()
	}
	
	override fun onClickedItem(response: Response) {
		if (response.syncState == SyncState.SENT.value) {
			Toast.makeText(context, getString(R.string.can_not_open_the_report), Toast.LENGTH_SHORT).show()
		} else {
			listener.openCreateResponse(response)
		}
	}
}
