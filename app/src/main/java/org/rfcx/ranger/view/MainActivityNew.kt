package org.rfcx.ranger.view

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.Gravity
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.android.synthetic.main.activity_main_new.*
import kotlinx.android.synthetic.main.layout_bottom_navigation_menu.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.rfcx.ranger.R
import org.rfcx.ranger.entity.report.Report
import org.rfcx.ranger.service.AirplaneModeReceiver
import org.rfcx.ranger.service.AlertNotification
import org.rfcx.ranger.util.*
import org.rfcx.ranger.view.alerts.AlertsFragment
import org.rfcx.ranger.view.base.BaseActivity
import org.rfcx.ranger.view.events.NewEventsFragment
import org.rfcx.ranger.view.map.MapFragment
import org.rfcx.ranger.view.profile.ProfileFragment
import org.rfcx.ranger.view.profile.ProfileViewModel.Companion.DOWNLOADING_STATE
import org.rfcx.ranger.view.profile.ProfileViewModel.Companion.DOWNLOAD_CANCEL_STATE
import org.rfcx.ranger.view.report.ReportActivity
import org.rfcx.ranger.widget.BottomNavigationMenuItem


// TODO change class name
class MainActivityNew : BaseActivity(), MainActivityEventListener, MainActivityListener {
	
	private val locationTrackingViewModel: LocationTrackingViewModel by viewModel()
	private val mainViewModel: MainActivityViewModel by viewModel()
	
	private lateinit var bottomSheetBehavior: BottomSheetBehavior<*>
	private val locationPermissions by lazy { LocationPermissions(this) }
	private val analytics by lazy { Analytics(this) }
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
		
		newReportFabButton.setOnClickListener {
			analytics.trackStartToAddReportEvent()
			ReportActivity.startIntent(this)
		}
		
		bottomSheetBehavior = BottomSheetBehavior.from(bottomSheetContainer)
		bottomSheetBehavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
			override fun onSlide(bottomSheet: View, slideOffset: Float) {
			
			}
			
			override fun onStateChanged(bottomSheet: View, newState: Int) {
				if (newState == BottomSheetBehavior.STATE_COLLAPSED) {
					showBottomAppBar()
					val bottomSheetFragment = supportFragmentManager.findFragmentByTag("BottomSheet")
					if (bottomSheetFragment != null) {
						supportFragmentManager.beginTransaction()
								.remove(bottomSheetFragment)
								.commit()
					}
				}
			}
			
		})
		
		observeMain()
		observeLocationTracking()
		observeEventFromNotification()
		
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
		
		if (bottomSheetBehavior.state == BottomSheetBehavior.STATE_EXPANDED) {
			hideBottomSheet()
		} else {
			return super.onBackPressed()
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
		
		menuMap.setOnClickListener {
			onBottomMenuClick(it)
		}
		
		menuAlert.setOnClickListener {
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
				menuMap.menuSelected = false
				menuAlert.menuSelected = false
				menuProfile.menuSelected = false
				
				showStatus()
			}
			menuMap.id -> {
				menuNewEvents.menuSelected = false
				menuMap.menuSelected = true
				menuAlert.menuSelected = false
				menuProfile.menuSelected = false
				
				showMap()
			}
			menuAlert.id -> {
				menuNewEvents.menuSelected = false
				menuMap.menuSelected = false
				menuAlert.menuSelected = true
				menuProfile.menuSelected = false
				
				showAlerts()
			}
			
			menuProfile.id -> {
				menuNewEvents.menuSelected = false
				menuMap.menuSelected = false
				menuAlert.menuSelected = false
				menuProfile.menuSelected = true
				
				showProfile()
			}
		}
	}
	
	override fun showBottomSheet(fragment: Fragment) {
		hidBottomAppBar()
		val layoutParams: CoordinatorLayout.LayoutParams = bottomSheetContainer.layoutParams
				as CoordinatorLayout.LayoutParams
		layoutParams.anchorGravity = Gravity.BOTTOM
		bottomSheetContainer.layoutParams = layoutParams
		supportFragmentManager.beginTransaction()
				.replace(bottomSheetContainer.id, fragment, "BottomSheet")
				.commit()
		bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
	}
	
	override fun hideBottomSheet() {
		bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
	}
	
	override fun hidBottomAppBar() {
		newReportFabButton.visibility = View.INVISIBLE
		bottomBar.visibility = View.INVISIBLE
	}
	
	override fun showBottomAppBar() {
		bottomBar.visibility = View.VISIBLE
		newReportFabButton.visibility = View.VISIBLE
	}
	
	override fun alertScreen() {
		menuAlert.performClick()
	}
	
	override fun moveMapIntoReportMarker(report: Report) {
		val mapFragment = supportFragmentManager.findFragmentByTag(MapFragment.tag)
		if (mapFragment is MapFragment) {
			mapFragment.moveToReportMarker(report)
		}
	}
	
	private fun startFragment(fragment: Fragment, showAboveAppbar: Boolean) {
		this.currentFragment = fragment
		showAboveAppbar(showAboveAppbar)
		supportFragmentManager.beginTransaction()
				.replace(contentContainer.id, fragment)
				.commit()
	}
	
	private fun setupFragments() {
		supportFragmentManager.beginTransaction()
				.add(contentContainer.id, getProfile(), ProfileFragment.tag)
				.add(contentContainer.id, getMap(), MapFragment.tag)
				.add(contentContainer.id, getAlerts(), AlertsFragment.tag)
				.add(contentContainer.id, getNewEvents(), NewEventsFragment.tag)
				.commit()
		
		menuNewEvents.performClick()
	}
	
	private fun getNewEvents(): NewEventsFragment = supportFragmentManager.findFragmentByTag(NewEventsFragment.tag)
			as NewEventsFragment? ?: NewEventsFragment.newInstance()
	
	private fun getMap(): MapFragment = supportFragmentManager.findFragmentByTag(MapFragment.tag)
			as MapFragment? ?: MapFragment.newInstance()
	
	private fun getAlerts(): AlertsFragment = supportFragmentManager.findFragmentByTag(AlertsFragment.tag)
			as AlertsFragment? ?: AlertsFragment.newInstance(null, 0)
	
	private fun getProfile(): ProfileFragment = supportFragmentManager.findFragmentByTag(ProfileFragment.tag)
			as ProfileFragment? ?: ProfileFragment.newInstance()
	
	private fun showStatus() {
		showAboveAppbar(true)
		this.currentFragment = getNewEvents()
		supportFragmentManager.beginTransaction()
				.show(getNewEvents())
				.hide(getMap())
				.hide(getAlerts())
				.hide(getProfile())
				.commit()
	}
	
	private fun showAlerts() {
		showAboveAppbar(true)
		this.currentFragment = getAlerts()
		supportFragmentManager.beginTransaction()
				.show(getAlerts())
				.hide(getNewEvents())
				.hide(getMap())
				.hide(getProfile())
				.commit()
	}
	
	private fun showMap() {
		showAboveAppbar(false)
		this.currentFragment = getMap()
		supportFragmentManager.beginTransaction()
				.show(getMap())
				.hide(getNewEvents())
				.hide(getAlerts())
				.hide(getProfile())
				.commit()
	}
	
	private fun showProfile() {
		showAboveAppbar(true)
		this.currentFragment = getProfile()
		supportFragmentManager.beginTransaction()
				.show(getProfile())
				.hide(getNewEvents())
				.hide(getAlerts())
				.hide(getMap())
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
		
		mainViewModel.alertCount.observe(this, Observer {
			menuAlert.badgeNumber = it
		})
	}
	
	private fun observeEventFromNotification() {
		mainViewModel.eventGuIdFromNotification.observe(this, Observer {
			
			val alertsFragment =
					supportFragmentManager.findFragmentByTag(AlertsFragment.tag)
			if (alertsFragment != null && alertsFragment is AlertsFragment) {
				alertsFragment.showDetail(it, EventItem.State.NONE)
			} else {
				menuNewEvents.menuSelected = false
				menuMap.menuSelected = false
				menuAlert.menuSelected = true
				menuProfile.menuSelected = false
				startFragment(AlertsFragment.newInstance(it, 1), true)
			}
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
	fun hidBottomAppBar()
	fun showBottomAppBar()
	fun alertScreen()
	fun moveMapIntoReportMarker(report: Report)
}

interface MainActivityListener {
	fun alertScreen()
}
