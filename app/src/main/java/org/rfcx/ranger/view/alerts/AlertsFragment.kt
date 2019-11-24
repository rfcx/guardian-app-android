package org.rfcx.ranger.view.alerts

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.google.android.material.tabs.TabLayout
import kotlinx.android.synthetic.main.fragment_alerts.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.rfcx.ranger.R
import org.rfcx.ranger.service.AlertNotification
import org.rfcx.ranger.util.Analytics
import org.rfcx.ranger.util.Screen
import org.rfcx.ranger.view.alert.AlertBottomDialogFragment
import org.rfcx.ranger.view.alert.AlertListener
import org.rfcx.ranger.view.base.BaseFragment

class AlertsFragment : BaseFragment(), AlertListener, AlertsNewInstanceListener {
	
	private val alertViewModel: AlertViewModel by viewModel()
	private val analytics by lazy { context?.let { Analytics(it) } }
	
	private val observeGuardianGroup = Observer<Boolean> {
		if (it) {
			val tabSelected = alertsTabLayout.selectedTabPosition
			startTabSelected(tabSelected)
		}
	}
	
	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
		return inflater.inflate(R.layout.fragment_alerts, container, false)
	}
	
	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		initView()
		observeAlert()
		getEventExtra()
		alertViewModel.loadAlerts()
	}
	
	override fun onResume() {
		super.onResume()
		alertViewModel.resumed()
		analytics?.trackScreen(Screen.ALERT)
	}
	
	private fun getEventExtra() {
		if (arguments?.containsKey(AlertNotification.ALERT_ID_NOTI_INTENT) == true) {
			arguments?.let {
				alertViewModel.eventIdFromNotification.value =
						it.getString(AlertNotification.ALERT_ID_NOTI_INTENT)
			}
		}
	}
	
	private fun observeAlert() {
		alertViewModel.eventIdFromNotification.observe(this, Observer {
			showDetail(it)
		})
	}
	
	override fun showDetail(eventGuID: String) {
		val currentShowing =
				childFragmentManager.findFragmentByTag(AlertBottomDialogFragment.tag)
		if (currentShowing != null && currentShowing is AlertBottomDialogFragment) {
			currentShowing.dismissDialog()
		}
		AlertBottomDialogFragment.newInstance(eventGuID).show(childFragmentManager,
				AlertBottomDialogFragment.tag)
	}
	
	override fun onReviewed(eventGuID: String, reviewValue: String) {
		val all = childFragmentManager.findFragmentByTag(AllAlertsFragment.tag)
		if (all is AllAlertsFragment) {
			all.onReviewed(eventGuID, reviewValue)
		}
	}
	
	private fun initView() {
		alertsTabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
			override fun onTabReselected(tab: TabLayout.Tab?) {
				if (tab != null) {
					startTabSelected(tab.position)
				}
			}
			
			override fun onTabUnselected(tab: TabLayout.Tab?) {
			
			}
			
			override fun onTabSelected(tab: TabLayout.Tab?) {
				if (tab != null) {
					startTabSelected(tab.position)
				}
			}
		})
		alertsTabLayout.getTabAt(0)?.select()
	}
	
	private fun startTabSelected(position: Int) {
		when (position) {
			0 -> {
				startFragment(GroupAlertsFragment.newInstance(), GroupAlertsFragment.tag)
			}
			1 -> {
				startFragment(AllAlertsFragment.newInstance(), AllAlertsFragment.tag)
			}
		}
	}
	
	override fun emptyAlert() {
		startFragment(EmptyAlertFragment.newInstance(), EmptyAlertFragment.tag)
	}
	
	private fun startFragment(fragment: Fragment, tag: String) {
		val startFragment = if (!alertViewModel.hasGuardianGroup) {
			observeSettingGuardianGroup() // start observe setting guardian group
			SetGuardianGroupFragment()
		} else {
			fragment
		}
		childFragmentManager.beginTransaction()
				.replace(contentContainer.id, startFragment,
						tag).commit()
	}
	
	private fun observeSettingGuardianGroup() {
		alertViewModel.observeGuardianGroup.removeObserver(observeGuardianGroup)
		alertViewModel.observeGuardianGroup.observe(this, observeGuardianGroup)
	}
	
	companion object {
		const val tag = "AlertsFragment"
		fun newInstance(eventGuId: String?): AlertsFragment {
			return AlertsFragment().apply {
				if (eventGuId != null) {
					arguments = Bundle().apply {
						putString(AlertNotification.ALERT_ID_NOTI_INTENT, eventGuId)
					}
				}
			}
		}
	}
}

interface AlertsNewInstanceListener {
	fun emptyAlert()
}