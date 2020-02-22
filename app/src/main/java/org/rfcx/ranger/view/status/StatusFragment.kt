package org.rfcx.ranger.view.status

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_status.*
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.rfcx.ranger.R
import org.rfcx.ranger.databinding.FragmentStatusBinding
import org.rfcx.ranger.entity.event.Event
import org.rfcx.ranger.entity.report.Report
import org.rfcx.ranger.util.Analytics
import org.rfcx.ranger.util.EventItem
import org.rfcx.ranger.util.Screen
import org.rfcx.ranger.view.LocationTrackingViewModel
import org.rfcx.ranger.view.MainActivityListener
import org.rfcx.ranger.view.alert.AlertBottomDialogFragment
import org.rfcx.ranger.view.alert.AlertListener
import org.rfcx.ranger.view.base.BaseFragment
import org.rfcx.ranger.view.profile.GuardianGroupActivity
import org.rfcx.ranger.view.report.ReportDetailActivity
import org.rfcx.ranger.view.status.adapter.StatusAdapter

class StatusFragment : BaseFragment(), StatusFragmentListener, AlertListener, MainActivityListener {
	
	private lateinit var viewDataBinding: FragmentStatusBinding
	private val statusViewModel: StatusViewModel by viewModel()
	private val locationTrackingViewModel: LocationTrackingViewModel by sharedViewModel()
	private val analytics by lazy { context?.let { Analytics(it) } }
	
	private val statusAdapter by lazy {
		StatusAdapter(context?.getString(R.string.status_stat_title),
				context?.getString(R.string.status_alert_title), context?.getString(R.string.status_report_title), context?.getString(R.string.status_see_more), context)
	}
	
	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
		viewDataBinding = DataBindingUtil.inflate(layoutInflater, R.layout.fragment_status, container, false)
		viewDataBinding.lifecycleOwner = this
		return viewDataBinding.root
	}
	
	override fun onResume() {
		super.onResume()
		analytics?.trackScreen(Screen.STATUS)
		statusViewModel.resumed()
		statusAdapter.update()
	}
	
	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		
		// setup recycler view
		statusAdapter.setListener(this)
		rvStatus?.apply {
			layoutManager = LinearLayoutManager(context)
			adapter = statusAdapter
		}
		
		statusViewModel.profile.observe(this, Observer {
			statusAdapter.updateHeader(it)
		})
		
		statusViewModel.summaryStat.observe(this, Observer {
			statusAdapter.updateStat(it)
		})
		
		statusViewModel.reportItems.observe(this, Observer {
			statusAdapter.updateReportList(it)
		})
		
		statusViewModel.alertItems.observe(this, Observer {
			statusAdapter.updateAlertList(it)
		})
		
		statusViewModel.syncInfo.observe(this, Observer {
			statusAdapter.updateSyncInfo(it)
		})
		
		statusViewModel.locationTracking.observe(this, Observer {
		
		})
		
		locationTrackingViewModel.locationTrackingState.observe(this, Observer {
			statusViewModel.updateTracking()
		})
	}
	
	override fun enableTracking(enable: Boolean) {
		if (enable) {
			// on location tracking
			locationTrackingViewModel.requireEnableLocationTracking()
		} else {
			// off location tracking
			locationTrackingViewModel.requireDisableLocationTracking()
		}
	}
	
	override fun onClickedReportItem(report: Report) {
		analytics?.trackSeeReportDetailEvent(report.id.toString(), report.value)
		ReportDetailActivity.startIntent(context, reportId = report.id)
	}
	
	override fun onClickedAlertItem(alert: Event, state: EventItem.State) {
		showDetail(alert.id, state)
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
	
	override fun onReviewed(reviewValue: String, event: Event) {
		statusViewModel.onEventReviewed(event, reviewValue)
	}
	
	override fun onClickedSeeMore() {
		alertScreen()
	}
	
	override fun alertScreen() {
		(activity as MainActivityListener).alertScreen()
	}
	
	override fun onClickedSetGuardianGroup() {
		analytics?.trackSetGuardianGroupStartEvent(Screen.STATUS)
		context?.let { it1 -> GuardianGroupActivity.startActivity(it1) }
	}
	
	companion object {
		fun newInstance(): StatusFragment {
			return StatusFragment()
		}
		
		const val tag = "StatusFragment"
	}
}

interface StatusFragmentListener {
	fun enableTracking(enable: Boolean)
	fun onClickedReportItem(report: Report)
	fun onClickedAlertItem(alert: Event, state: EventItem.State)
	fun onClickedSeeMore()
	fun onClickedSetGuardianGroup()
}