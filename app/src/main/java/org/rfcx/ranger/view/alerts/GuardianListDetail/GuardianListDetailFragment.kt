package org.rfcx.ranger.view.alerts.GuardianListDetail

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_guardian_list_detail.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.rfcx.ranger.R
import org.rfcx.ranger.data.remote.success
import org.rfcx.ranger.entity.event.Event
import org.rfcx.ranger.util.handleError
import org.rfcx.ranger.view.alert.AlertBottomDialogFragment
import org.rfcx.ranger.view.alert.AlertListener
import org.rfcx.ranger.view.alerts.adapter.AlertClickListener
import org.rfcx.ranger.view.base.BaseFragment

class GuardianListDetailFragment : BaseFragment(), AlertClickListener, AlertListener {
	private val viewModel: GuardianListDetailViewModel by viewModel()
	private val guardianListDetailAdapter by lazy { GuardianListDetailAdapter(this) }
	
	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
		return inflater.inflate(R.layout.fragment_guardian_list_detail, container, false)
	}
	
	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		setupAlertList()
		
		val event = arguments?.getParcelableArrayList<Event>("events")
		event?.let { viewModel.makeGroupOfValue(it) }
		
		viewModel.items.observe(this, Observer { it ->
			it.success({ items ->
				loadingProgress.visibility = View.INVISIBLE
				guardianListDetailAdapter.allItem = items
			}, {
				loadingProgress.visibility = View.INVISIBLE
				context.handleError(it)
			}, {
				loadingProgress.visibility = View.VISIBLE
			})
		})
		
		viewModel.loading.observe(this, Observer {
			if(it == StateLoading.LOADING){
				loadingProgress.visibility = View.VISIBLE
			} else if (it == StateLoading.NOT_LOADING){
				loadingProgress.visibility = View.INVISIBLE
			}
		})
		
		guardianListDetailAdapter.mOnSeeOlderClickListener = object : OnSeeOlderClickListener {
			override fun onSeeOlderClick(guid: String, value: String, endAt: String) {
				Log.d("onSeeOlderClick FM", "$guid $value $endAt")
				viewModel.loadMoreEvents(guid, value, endAt)
			}
		}
	}
	
	private fun setupAlertList() {
		val alertsLayoutManager = LinearLayoutManager(context)
		eventsInGuardianRecycler.apply {
			layoutManager = alertsLayoutManager
			adapter = guardianListDetailAdapter
		}
	}
	
	override fun onClickedAlert(event: Event) {
		showDetail(event)
	}
	
	override fun showDetail(event: Event) {
		val currentShowing =
				childFragmentManager.findFragmentByTag(AlertBottomDialogFragment.tag)
		if (currentShowing != null && currentShowing is AlertBottomDialogFragment) {
			currentShowing.dismissAllowingStateLoss()
		}
		AlertBottomDialogFragment.newInstance(event).show(childFragmentManager,
				AlertBottomDialogFragment.tag)
	}
	
	override fun onReviewed(eventGuID: String, reviewValue: String) {
		viewModel.onEventReviewed(eventGuID, reviewValue)
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

interface OnSeeOlderClickListener {
	fun onSeeOlderClick(guid: String, value: String, endAt: String)
}