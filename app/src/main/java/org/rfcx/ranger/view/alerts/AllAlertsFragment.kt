package org.rfcx.ranger.view.alerts

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.fragment_all_alerts.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.rfcx.ranger.R
import org.rfcx.ranger.adapter.entity.BaseItem
import org.rfcx.ranger.data.remote.success
import org.rfcx.ranger.entity.event.Event
import org.rfcx.ranger.util.Analytics
import org.rfcx.ranger.util.EventItem
import org.rfcx.ranger.util.handleError
import org.rfcx.ranger.view.alert.AlertListener
import org.rfcx.ranger.view.alerts.adapter.AlertClickListener
import org.rfcx.ranger.view.alerts.adapter.AlertsAdapter
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
		setupSwipeRefresh()
		
		allAlertsViewModel.alerts.observe(this, Observer { it ->
			it.success({ items ->
				alertsSwipeRefresh.isRefreshing = false
				if (items.isEmpty()) {
					(parentFragment as AlertsNewInstanceListener?)?.emptyAlert()
				} else {
					val newList = mutableListOf<BaseItem>()
					items.forEach { item -> newList.add(item.copy()) }
					alertsAdapter.submitList(newList)
				}
			}, {
				alertsSwipeRefresh.isRefreshing = false
				context?.handleError(it)
			}, {
				alertsSwipeRefresh.isRefreshing = !allAlertsViewModel.isLoadMore
				
				if (allAlertsViewModel.isLoadMore) {
					alertsAdapter.submitList(allAlertsViewModel.getItemsWithLoading())
				}
			})
		})
	}
	
	private fun setupSwipeRefresh() {
		context?.let { alertsSwipeRefresh.setColorSchemeColors(ContextCompat.getColor(it,
				R.color.colorPrimary))}
		alertsSwipeRefresh.setOnRefreshListener {
			allAlertsViewModel.refresh()
		}
	}
	
	override fun onClickedAlert(event: Event, state: EventItem.State) {
		(parentFragment as AlertListener?)?.showDetail(event.id, state)
		event.value.let { analytics?.trackSeeAlertDetailEvent(event.id, it) }
	}
	
	fun onReviewed(reviewValue: String, event: Event) {
		allAlertsViewModel.onEventReviewed(reviewValue, event)
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
					if (!alertsSwipeRefresh.isRefreshing) {
						if ((visibleItemCount + firstVisibleItemPosition) >= total
								&& firstVisibleItemPosition >= 0
								&& total >= AllAlertsViewModel.PAGE_LIMITS
								&& !allAlertsViewModel.isLoadMore) {
							
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