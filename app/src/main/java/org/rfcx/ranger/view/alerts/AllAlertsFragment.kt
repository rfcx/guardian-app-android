package org.rfcx.ranger.view.alerts

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.fragment_all_alerts.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.rfcx.ranger.R
import org.rfcx.ranger.data.remote.success
import org.rfcx.ranger.entity.event.Event
import org.rfcx.ranger.util.Analytics
import org.rfcx.ranger.util.handleError
import org.rfcx.ranger.view.alert.AlertListener
import org.rfcx.ranger.view.alerts.adapter.AlertClickListener
import org.rfcx.ranger.view.alerts.adapter.AlertsAdapter
import org.rfcx.ranger.view.alerts.adapter.EventItem
import org.rfcx.ranger.view.base.BaseFragment

class AllAlertsFragment : BaseFragment(), AlertClickListener {
	
	private val allAlertsViewModel: AllAlertsViewModel by viewModel()
	private val analytics by lazy { context?.let { Analytics(it) } }
	
	private val alertsAdapter by lazy {
		AlertsAdapter(this)
	}
	
	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
		return inflater.inflate(R.layout.fragment_all_alerts, container, false)
	}
	
	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		setupAlertList()
		
		allAlertsViewModel.alerts.observe(this, Observer { it ->
			
			it.success({ items ->
				val newList = mutableListOf<EventItem>()
				items.forEach { item -> newList.add(item.copy()) }
				alertsAdapter.submitList(newList)
				loadingProgress.visibility = View.INVISIBLE
			}, {
				loadingProgress.visibility = View.INVISIBLE
				context?.handleError(it)
			}, {
				loadingProgress.visibility = View.VISIBLE
			})
		})
		
		allAlertsViewModel.loadEvents()
	}
	
	override fun onClickedAlert(event: Event) {
		(parentFragment as AlertListener?)?.showDetail(event)
		event.value?.let { analytics?.trackSeeAlertDetailEvent(event.event_guid, it) }
	}
	
	fun onReviewed(eventGuID: String, reviewValue: String) {
		allAlertsViewModel.onEventReviewed(eventGuID, reviewValue)
	}
	
	private fun setupAlertList() {
		val alertsLayoutManager = LinearLayoutManager(context)
		alertsRecyclerView?.apply {
			layoutManager = alertsLayoutManager
			adapter = alertsAdapter
			addOnScrollListener(object : RecyclerView.OnScrollListener() {
				override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
					super.onScrolled(recyclerView, dx, dy)
					val visibleItemCount = alertsLayoutManager.childCount
					val total = alertsLayoutManager.itemCount
					val firstVisibleItemPosition = alertsLayoutManager.findFirstVisibleItemPosition()
					if (loadingProgress.visibility != View.VISIBLE && !allAlertsViewModel.isLastPage) {
						if ((visibleItemCount + firstVisibleItemPosition) >= total
								&& firstVisibleItemPosition >= 0
								&& total >= AllAlertsViewModel.PAGE_LIMITS) {
							
							// load events
							allAlertsViewModel.loadMoreEvents()
						}
					}
				}
			})
		}
	}
	
	companion object {
		const val tag = "AllAlertsFragment"
		fun newInstance(): AllAlertsFragment {
			return AllAlertsFragment()
		}
	}
}