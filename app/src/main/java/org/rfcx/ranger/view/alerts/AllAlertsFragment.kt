package org.rfcx.ranger.view.alerts

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_all_alerts.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.rfcx.ranger.R
import org.rfcx.ranger.entity.event.Event
import org.rfcx.ranger.view.alert.AlertBottomDialogFragment
import org.rfcx.ranger.view.alert.ReviewAlertCallback
import org.rfcx.ranger.view.alerts.adapter.AlertClickListener
import org.rfcx.ranger.view.alerts.adapter.AlertsAdapter
import org.rfcx.ranger.view.base.BaseFragment

class AllAlertsFragment : BaseFragment(), AlertClickListener , ReviewAlertCallback{
	
	private val alertsViewModel: AlertsViewModel by viewModel()
	private val alertsAdapter by lazy {
		AlertsAdapter(this)
	}
	
	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
		
		return inflater.inflate(R.layout.fragment_all_alerts, container, false)
	}
	
	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		setupAlertList()
		
		alertsViewModel.loading.observe(this, Observer {
			loadingProgress.visibility = if (it) View.VISIBLE else View.INVISIBLE
		})
		
		alertsViewModel.alerts.observe(this, Observer {
			alertsAdapter.items = it
		})
		
		alertsViewModel.loadEvents()
	}
	
	override fun onClickedAlert(event: Event) {
		AlertBottomDialogFragment.newInstance(event).show(childFragmentManager,
				AlertBottomDialogFragment.tag)
	}
	
	override fun onReviewed(eventGuID: String, reviewValue: String) {
		// TODO update ui with eventGuID and reviewValue
		// reviewValue contains of confirm and reject
		
	}
	
	private fun setupAlertList() {
		val alertsLayoutManager = LinearLayoutManager(context)
		alertsRecyclerView?.apply {
			adapter = alertsAdapter
			layoutManager = alertsLayoutManager
//            addOnScrollListener(object : RecyclerView.OnScrollListener() {
//                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
//                    super.onScrolled(recyclerView, dx, dy)
//                    val visibleItemCount = alertsLayoutManager.childCount
//                    val total = alertsLayoutManager.itemCount
//                    val firstVisibleItemPosition = alertsLayoutManager.findFirstVisibleItemPosition()
//                    if ((visibleItemCount + firstVisibleItemPosition) >= total
//                            && firstVisibleItemPosition >= 0
//                            && total >= AlertsViewModel.PAGE_LIMITS) {
//
//                        // load events
//                        alertsViewModel.loadEvents()
//                    }
//                }
//            })
		}
	}
	
	companion object {
		const val tag = "AllAlertsFragment"
		fun newInstance(): AllAlertsFragment {
			return AllAlertsFragment()
		}
	}
}