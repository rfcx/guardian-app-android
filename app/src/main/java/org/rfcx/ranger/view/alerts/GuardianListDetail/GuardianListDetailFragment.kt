package org.rfcx.ranger.view.alerts.GuardianListDetail

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_guardian_list_detail.*
import org.rfcx.ranger.R
import org.rfcx.ranger.entity.event.Event
import org.rfcx.ranger.view.base.BaseFragment
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.rfcx.ranger.data.remote.success
import org.rfcx.ranger.util.handleError

class GuardianListDetailFragment : BaseFragment() {
	
	private val viewModel: GuardianListDetailViewModel by viewModel()
	private val guardianListDetailAdapter by lazy { GuardianListDetailAdapter() }
	
	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
		return inflater.inflate(R.layout.fragment_guardian_list_detail, container, false)
	}
	
	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		
		val event = arguments?.getParcelableArrayList<Event>("events")
		event?.let { viewModel.makeGroupOfValue(it) }
		
		eventsInGuardianRecycler.apply {
			layoutManager = LinearLayoutManager(context)
			adapter = guardianListDetailAdapter
		}
		
		viewModel.items.observe(this, Observer { it ->
			it.success({
				loadingProgress.visibility = View.INVISIBLE
				guardianListDetailAdapter.allItem = viewModel.eventAll
			}, {
				loadingProgress.visibility = View.INVISIBLE
				context.handleError(it)
			}, {
				loadingProgress.visibility = View.VISIBLE
			})
		})
		
	}
	
	companion object {
		const val tag = "GuardianListDetailFragment"
		
		fun newInstance(events: ArrayList<Event>): GuardianListDetailFragment {
			return GuardianListDetailFragment().apply {
				arguments = Bundle().apply {
					putParcelableArrayList("events", events)
				}
			}
		}
	}
}