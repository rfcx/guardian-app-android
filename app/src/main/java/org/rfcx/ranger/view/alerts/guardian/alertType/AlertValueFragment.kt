package org.rfcx.ranger.view.alerts.guardian.alertType

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.fragment_alert_detail_by_type.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.rfcx.ranger.R
import org.rfcx.ranger.adapter.entity.BaseItem
import org.rfcx.ranger.data.remote.success
import org.rfcx.ranger.entity.event.Event
import org.rfcx.ranger.util.EventItem
import org.rfcx.ranger.view.alert.AlertBottomDialogFragment
import org.rfcx.ranger.view.alert.AlertListener
import org.rfcx.ranger.view.alerts.adapter.AlertClickListener
import org.rfcx.ranger.view.alerts.guardian.alertType.AlertValueActivity.Companion.EXTRA_ALERT_VALUE
import org.rfcx.ranger.view.alerts.guardian.alertType.AlertValueActivity.Companion.EXTRA_GUARDIAN_NAME
import org.rfcx.ranger.view.base.BaseFragment

class AlertValueFragment : BaseFragment(), AlertClickListener, AlertListener {
	private val viewModel: AlertValueViewModel by viewModel()
	private val alertByValueAdapter by lazy {
		AlertByValueAdapter(this)
	}
	
	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
		return inflater.inflate(R.layout.fragment_alert_detail_by_type, container, false)
	}
	
	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		
		setupAlertList()
		
		val value = arguments?.getString(EXTRA_ALERT_VALUE)
		val guardianName = arguments?.getString(EXTRA_GUARDIAN_NAME)
		if (value != null && guardianName != null) {
			viewModel.getEvents(value, guardianName)
		}
		
		alertDetailByTypeRecycler.apply {
			layoutManager = LinearLayoutManager(context)
			adapter = alertByValueAdapter
		}
		
		viewModel.baseItems.observe(this, Observer {
			it.success({ items ->
				val newList = mutableListOf<BaseItem>()
				items.mapTo(newList, { item -> if (item is EventItem) item.copy() else item })
				alertByValueAdapter.submitList(newList)
			})
		})
	}
	
	override fun onClickedAlert(event: Event, state: EventItem.State) {
		showDetail(event.id, state)
	}
	
	override fun showDetail(eventGuID: String, state: EventItem.State) {
		val currentShowing =
				childFragmentManager.findFragmentByTag(AlertBottomDialogFragment.tag)
		if (currentShowing != null && currentShowing is AlertBottomDialogFragment) {
			currentShowing.dismissAllowingStateLoss()
		}
		AlertBottomDialogFragment.newInstance(eventGuID, state).show(childFragmentManager,
				AlertBottomDialogFragment.tag)
	}
	
	override fun onReviewed(reviewValue: String, event: Event) {
		viewModel.onEventReviewed(event, reviewValue)
	}
	
	private fun setupAlertList() {
		val alertsDetailLayoutManager = LinearLayoutManager(context)
		
		alertDetailByTypeRecycler?.apply {
			layoutManager = alertsDetailLayoutManager
			adapter = alertByValueAdapter
			
			addOnScrollListener(object : RecyclerView.OnScrollListener() {
				override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
					super.onScrolled(recyclerView, dx, dy)
					val visibleItemCount = (layoutManager as LinearLayoutManager).childCount
					val total = (layoutManager as LinearLayoutManager).itemCount
					val firstVisibleItemPosition = (layoutManager as LinearLayoutManager).findLastVisibleItemPosition()
					
					if ((visibleItemCount + firstVisibleItemPosition) >= total
							&& firstVisibleItemPosition >= 0
							&& !viewModel.isLoadMore) {
						
						// load events
						viewModel.loadMoreEvents()
					}
				}
			})
		}
	}
	
	companion object {
		const val tag = "AlertDetailByTypeFragment"
		fun newInstance(value: String, guardianName: String): AlertValueFragment {
			return AlertValueFragment().apply {
				arguments = Bundle().apply {
					putString(EXTRA_ALERT_VALUE, value)
					putString(EXTRA_GUARDIAN_NAME, guardianName)
				}
			}
		}
	}
}