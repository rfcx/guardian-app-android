package org.rfcx.ranger.view.report.submitted

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_submitted_reports.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.rfcx.ranger.R
import org.rfcx.ranger.view.MainActivityViewModel

class SubmittedReportsFragment : Fragment() {
	private val viewModel: MainActivityViewModel by viewModel()
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
		setObserve()
	}
	
	@SuppressLint("NotifyDataSetChanged")
	private fun setObserve() {
		viewModel.getResponses().observe(viewLifecycleOwner, { responses ->
			notHaveSubmittedReportsGroupView.visibility = if (responses.isEmpty()) View.VISIBLE else View.GONE
			reportsAdapter.items = responses.sortedByDescending { r -> r.investigatedAt }
			reportsAdapter.notifyDataSetChanged()
		})
	}
	
	companion object {
		const val tag = "SubmittedReportsFragment"
		
		@JvmStatic
		fun newInstance() = SubmittedReportsFragment()
	}
}
