package org.rfcx.incidents.view.report.submitted

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_submitted_reports.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.rfcx.incidents.R
import org.rfcx.incidents.entity.response.Response
import org.rfcx.incidents.entity.response.SyncState
import org.rfcx.incidents.util.Analytics
import org.rfcx.incidents.util.Screen
import org.rfcx.incidents.view.MainActivityEventListener
import org.rfcx.incidents.view.MainActivityViewModel

class SubmittedReportsFragment : Fragment(), SubmittedReportsOnClickListener {
	private val analytics by lazy { context?.let { Analytics(it) } }
	private val viewModel: MainActivityViewModel by viewModel()
	private val reportsAdapter by lazy { SubmittedReportsAdapter(this) }
	lateinit var listener: MainActivityEventListener
	
	override fun onAttach(context: Context) {
		super.onAttach(context)
		listener = (context as MainActivityEventListener)
	}
	
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
		setObserve()
	}
	
	override fun onResume() {
		super.onResume()
		analytics?.trackScreen(Screen.SUBMITTED_REPORTS)
	}
	
	@SuppressLint("NotifyDataSetChanged")
	private fun setObserve() {
		viewModel.getResponses().observe(viewLifecycleOwner, { responses ->
			notHaveSubmittedReportsGroupView.visibility = if (responses.isEmpty()) View.VISIBLE else View.GONE
			reportsAdapter.items = responses.sortedByDescending { r -> r.investigatedAt }
			reportsAdapter.notifyDataSetChanged()
		})
	}
	
	
	override fun onClickedItem(response: Response) {
		if (response.syncState == SyncState.SENT.value) {
			response.guid?.let { listener.openDetailResponse(it) }
		}
	}
	
	companion object {
		const val tag = "SubmittedReportsFragment"
		
		@JvmStatic
		fun newInstance() = SubmittedReportsFragment()
	}
}
