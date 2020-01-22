package org.rfcx.ranger.view.alerts.guardianListDetail

import android.os.Bundle
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
import org.rfcx.ranger.util.EventItem
import org.rfcx.ranger.util.handleError
import org.rfcx.ranger.view.alert.AlertBottomDialogFragment
import org.rfcx.ranger.view.alert.AlertListener
import org.rfcx.ranger.view.alerts.guardianListDetail.alertDetailByType.AlertDetailByTypeActivity
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
		
		val guardianName = arguments?.getString("GUARDIAN_NAME")
		if (guardianName != null) {
			viewModel.getEventFromDatabase(guardianName)
		}
		
		viewModel.arrayEventGroup.observe(this, Observer { it ->
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
		
		guardianListDetailAdapter.mOnItemViewClickListener = object : OnItemViewClickListener {
			override fun onItemViewClick(value: String, label: String, guardianName: String) {
				context?.let { AlertDetailByTypeActivity.startActivity(it, value, label, guardianName) }
			}
		}
	}
	
	override fun onResume() {
		super.onResume()
		val guardianName = arguments?.getString("GUARDIAN_NAME")
		if (guardianName != null) {
			viewModel.getEventFromDatabase(guardianName)
		}
	}
	
	private fun setupAlertList() {
		val alertsLayoutManager = LinearLayoutManager(context)
		eventsInGuardianRecycler.apply {
			layoutManager = alertsLayoutManager
			adapter = guardianListDetailAdapter
		}
	}
	
	override fun onClickedAlert(event: Event, state: EventItem.State) {
		showDetail(event.id, state)
	}
	
	override fun showDetail(eventGuID: String, state: EventItem.State) {
		val currentShowing =
				childFragmentManager.findFragmentByTag(AlertBottomDialogFragment.tag)
		if (currentShowing != null && currentShowing is AlertBottomDialogFragment) {
			currentShowing.dismissDialog()
		}
		AlertBottomDialogFragment.newInstance(eventGuID, state).show(childFragmentManager,
				AlertBottomDialogFragment.tag)
	}
	
	override fun onReviewed(eventGuID: String, reviewValue: String) {
		viewModel.onEventReviewed(eventGuID, reviewValue)
	}
	
	companion object {
		const val tag = "GuardianListDetailFragment"
		
		fun newInstance(guardianName: String): GuardianListDetailFragment {
			return GuardianListDetailFragment().apply {
				arguments = Bundle().apply {
					putString("GUARDIAN_NAME", guardianName)
				}
			}
		}
	}
}

interface OnItemViewClickListener {
	fun onItemViewClick(value: String, label: String, guardianName: String)
}