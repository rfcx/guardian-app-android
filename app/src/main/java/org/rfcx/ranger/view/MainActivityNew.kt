package org.rfcx.ranger.view

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import kotlinx.android.synthetic.main.activity_main_new.*
import kotlinx.android.synthetic.main.fragment_new_events.*
import kotlinx.android.synthetic.main.layout_bottom_navigation_menu.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.rfcx.ranger.R
import org.rfcx.ranger.entity.report.Report
import org.rfcx.ranger.service.AirplaneModeReceiver
import org.rfcx.ranger.service.AlertNotification
import org.rfcx.ranger.util.*
import org.rfcx.ranger.view.base.BaseActivity
import org.rfcx.ranger.view.events.EventsFragment
import org.rfcx.ranger.view.events.adapter.GuardianModel
import org.rfcx.ranger.view.events.detail.GuardianEventDetailFragment
import org.rfcx.ranger.view.map.MapFragment
import org.rfcx.ranger.view.profile.ProfileFragment
import org.rfcx.ranger.view.profile.ProfileViewModel.Companion.DOWNLOADING_STATE
import org.rfcx.ranger.view.profile.ProfileViewModel.Companion.DOWNLOAD_CANCEL_STATE
import org.rfcx.ranger.view.report.DraftReportsFragment
import org.rfcx.ranger.view.submitted.SubmittedReportsFragment
import org.rfcx.ranger.widget.BottomNavigationMenuItem


// TODO change class name
class MainActivityNew : BaseActivity(), MainActivityEventListener, MainActivityListener {
	private val locationTrackingViewModel: LocationTrackingViewModel by viewModel()
	private val mainViewModel: MainActivityViewModel by viewModel()
	
	private val locationPermissions by lazy { LocationPermissions(this) }
	private var currentFragment: Fragment? = null
	
	private val onAirplaneModeCallback: (Boolean) -> Unit = { isOnAirplaneMode ->
		if (isOnAirplaneMode) {
			showLocationError()
			LocationTracking.set(this, false)
			locationTrackingViewModel.trackingStateChange()
		}
	}
	private val airplaneModeReceiver = AirplaneModeReceiver(onAirplaneModeCallback)
	
	
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_main_new)
		
		val preferences = Preferences.getInstance(this)
		val state = preferences.getString(Preferences.OFFLINE_MAP_STATE)
		if (state == DOWNLOADING_STATE) {
			preferences.putString(Preferences.OFFLINE_MAP_STATE, DOWNLOAD_CANCEL_STATE)
		}
		setupBottomMenu()
		if (savedInstanceState == null) {
			setupFragments()
		}
		
		this.saveUserLoginWith()
		observeMain()
		observeLocationTracking()
		getEventFromIntentIfHave(intent)
	}
	
	override fun onResume() {
		registerReceiver(airplaneModeReceiver, IntentFilter(Intent.ACTION_AIRPLANE_MODE_CHANGED))
		super.onResume()
		mainViewModel.updateLocationTracking()
	}
	
	override fun onPause() {
		unregisterReceiver(airplaneModeReceiver)
		super.onPause()
	}
	
	override fun onNewIntent(intent: Intent?) {
		super.onNewIntent(intent)
		getEventFromIntentIfHave(intent)
	}
	
	override fun onBackPressed() {
		when (supportFragmentManager.findFragmentById(R.id.contentContainer)) {
			is GuardianEventDetailFragment -> {
				if (supportFragmentManager.backStackEntryCount > 0) {
					supportFragmentManager.popBackStack()
				} else {
					super.onBackPressed()
				}
				showBottomAppBar()
			}
		}
		
		projectRecyclerView?.let {
			if (it.visibility == View.VISIBLE) {
				showBottomAppBar()
				it.visibility = View.GONE
				projectSwipeRefreshView.visibility = View.GONE
			}
		}
	}
	
	override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults)
		locationPermissions.handleRequestResult(requestCode, grantResults)
		
		currentFragment?.let {
			if (it is MapFragment) {
				it.onRequestPermissionsResult(requestCode, permissions, grantResults)
			}
		}
	}
	
	override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
		super.onActivityResult(requestCode, resultCode, data)
		locationPermissions.handleActivityResult(requestCode, resultCode)
		
		currentFragment?.let {
			if (it is MapFragment) {
				it.onActivityResult(requestCode, resultCode, data)
			}
		}
	}
	
	private fun setupBottomMenu() {
		menuNewEvents.setOnClickListener {
			onBottomMenuClick(it)
		}
		
		menuSubmittedReports.setOnClickListener {
			onBottomMenuClick(it)
		}
		
		menuDraftReports.setOnClickListener {
			onBottomMenuClick(it)
		}
		
		menuProfile.setOnClickListener {
			onBottomMenuClick(it)
		}
		
		menuNewEvents.performClick()
	}
	
	private fun onBottomMenuClick(menu: View) {
		if ((menu as BottomNavigationMenuItem).menuSelected) return
		when (menu.id) {
			menuNewEvents.id -> {
				menuNewEvents.menuSelected = true
				menuSubmittedReports.menuSelected = false
				menuDraftReports.menuSelected = false
				menuProfile.menuSelected = false
				
				showStatus()
			}
			menuSubmittedReports.id -> {
				menuNewEvents.menuSelected = false
				menuSubmittedReports.menuSelected = true
				menuDraftReports.menuSelected = false
				menuProfile.menuSelected = false
				
				showMap()
			}
			menuDraftReports.id -> {
				menuNewEvents.menuSelected = false
				menuSubmittedReports.menuSelected = false
				menuDraftReports.menuSelected = true
				menuProfile.menuSelected = false
				
				showAlerts()
			}
			
			menuProfile.id -> {
				menuNewEvents.menuSelected = false
				menuSubmittedReports.menuSelected = false
				menuDraftReports.menuSelected = false
				menuProfile.menuSelected = true
				
				showProfile()
			}
		}
	}
	
	override fun showBottomSheet(fragment: Fragment) {}
	
	override fun hideBottomSheet() {}
	
	override fun hideBottomAppBar() {
		showAboveAppbar(false)
		bottomBar.visibility = View.GONE
	}
	
	override fun showBottomAppBar() {
		showAboveAppbar(true)
		bottomBar.visibility = View.VISIBLE
	}
	
	override fun alertScreen() {
		menuDraftReports.performClick()
	}
	
	override fun openGuardianEventDetail(item: GuardianModel) {
		hideBottomAppBar()
		startFragment(GuardianEventDetailFragment.newInstance(item.name, item.distance, item.numberOfAlerts), GuardianEventDetailFragment.tag)
	}
	
	private fun startFragment(fragment: Fragment, tag: String = "") {
		if (tag.isBlank()) {
			supportFragmentManager.beginTransaction()
					.replace(contentContainer.id, fragment)
					.commit()
		} else {
			supportFragmentManager.beginTransaction()
					.replace(contentContainer.id, fragment, tag)
					.addToBackStack(tag)
					.commit()
		}
	}
	
	override fun moveMapIntoReportMarker(report: Report) {
		val mapFragment = supportFragmentManager.findFragmentByTag(MapFragment.tag)
		if (mapFragment is MapFragment) {
			mapFragment.moveToReportMarker(report)
		}
	}
	
	private fun setupFragments() {
		supportFragmentManager.beginTransaction()
				.add(contentContainer.id, getProfile(), ProfileFragment.tag)
				.add(contentContainer.id, getSubmittedReports(), SubmittedReportsFragment.tag)
				.add(contentContainer.id, getDraftReports(), DraftReportsFragment.tag)
				.add(contentContainer.id, getNewEvents(), EventsFragment.tag)
				.commit()
		
		menuNewEvents.performClick()
	}
	
	private fun getNewEvents(): EventsFragment = supportFragmentManager.findFragmentByTag(EventsFragment.tag)
			as EventsFragment? ?: EventsFragment.newInstance()
	
	private fun getSubmittedReports(): SubmittedReportsFragment = supportFragmentManager.findFragmentByTag(SubmittedReportsFragment.tag)
			as SubmittedReportsFragment? ?: SubmittedReportsFragment.newInstance()
	
	private fun getDraftReports(): DraftReportsFragment = supportFragmentManager.findFragmentByTag(DraftReportsFragment.tag)
			as DraftReportsFragment? ?: DraftReportsFragment.newInstance()
	
	private fun getProfile(): ProfileFragment = supportFragmentManager.findFragmentByTag(ProfileFragment.tag)
			as ProfileFragment? ?: ProfileFragment.newInstance()
	
	private fun showStatus() {
		showAboveAppbar(true)
		this.currentFragment = getNewEvents()
		supportFragmentManager.beginTransaction()
				.show(getNewEvents())
				.hide(getSubmittedReports())
				.hide(getDraftReports())
				.hide(getProfile())
				.commit()
	}
	
	private fun showAlerts() {
		showAboveAppbar(true)
		this.currentFragment = getDraftReports()
		supportFragmentManager.beginTransaction()
				.show(getDraftReports())
				.hide(getNewEvents())
				.hide(getSubmittedReports())
				.hide(getProfile())
				.commit()
	}
	
	private fun showMap() {
		showAboveAppbar(false)
		this.currentFragment = getSubmittedReports()
		supportFragmentManager.beginTransaction()
				.show(getSubmittedReports())
				.hide(getNewEvents())
				.hide(getDraftReports())
				.hide(getProfile())
				.commit()
	}
	
	private fun showProfile() {
		showAboveAppbar(true)
		this.currentFragment = getProfile()
		supportFragmentManager.beginTransaction()
				.show(getProfile())
				.hide(getNewEvents())
				.hide(getDraftReports())
				.hide(getSubmittedReports())
				.commit()
	}
	
	private fun showAboveAppbar(show: Boolean) {
		val contentContainerPaddingBottom =
				if (show) resources.getDimensionPixelSize(R.dimen.bottom_bar_height) else 0
		contentContainer.setPadding(0, 0, 0, contentContainerPaddingBottom)
	}
	
	private fun observeLocationTracking() {
		locationTrackingViewModel.requireLocationTrackingState.observe(this, Observer {
			if (it) {
				enableLocationTracking()
			} else {
				disableLocationTracking()
			}
		})
	}
	
	private fun observeMain() {
		mainViewModel.isRequireToLogin.observe(this, Observer {
			if (it) logout()
		})
		
		mainViewModel.isLocationTrackingOn.observe(this, Observer {
			if (it) enableLocationTracking()
		})
	}
	
	private fun getEventFromIntentIfHave(intent: Intent?) {
		if (intent?.hasExtra(AlertNotification.ALERT_ID_NOTI_INTENT) == true) {
			val eventGuId: String? = intent.getStringExtra(AlertNotification.ALERT_ID_NOTI_INTENT)
			mainViewModel.eventGuIdFromNotification.value = eventGuId
		}
	}
	
	private fun enableLocationTracking() {
		if (isOnAirplaneMode()) {
			showLocationError()
			LocationTracking.set(this, false)
			locationTrackingViewModel.trackingStateChange()
		} else {
			locationPermissions.check { hasPermission: Boolean ->
				LocationTracking.set(this, hasPermission)
				locationTrackingViewModel.trackingStateChange()
			}
		}
	}
	
	private fun showLocationError() {
		AlertDialog.Builder(this)
				.setTitle(R.string.in_air_plane_mode)
				.setMessage(R.string.pls_off_air_plane_mode)
				.setPositiveButton(R.string.common_ok, null)
				.show()
	}
	
	private fun disableLocationTracking() {
		LocationTracking.set(this, false)
		locationTrackingViewModel.trackingStateChange()
	}
	
	companion object {
		fun startActivity(context: Context, eventGuId: String?) {
			val intent = Intent(context, MainActivityNew::class.java)
			if (eventGuId != null)
				intent.putExtra(AlertNotification.ALERT_ID_NOTI_INTENT, eventGuId)
			context.startActivity(intent)
		}
	}
}

interface MainActivityEventListener {
	fun showBottomSheet(fragment: Fragment)
	fun hideBottomSheet()
	fun hideBottomAppBar()
	fun showBottomAppBar()
	fun alertScreen()
	fun onBackPressed()
	fun openGuardianEventDetail(item: GuardianModel)
	fun moveMapIntoReportMarker(report: Report)
}

interface MainActivityListener {
	fun alertScreen()
}
