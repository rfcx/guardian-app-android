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
import kotlinx.android.synthetic.main.activity_main_new.contentContainer
import kotlinx.android.synthetic.main.fragment_alerts.*
import kotlinx.android.synthetic.main.layout_bottom_navigation_menu.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.rfcx.ranger.R
import org.rfcx.ranger.entity.report.Report
import org.rfcx.ranger.service.AirplaneModeReceiver
import org.rfcx.ranger.service.AlertNotification
import org.rfcx.ranger.util.*
import org.rfcx.ranger.view.alerts.AlertsFragment
import org.rfcx.ranger.view.base.BaseActivity
import org.rfcx.ranger.view.map.MapFragment
import org.rfcx.ranger.view.profile.ProfileFragment
import org.rfcx.ranger.view.report.ReportActivity
import org.rfcx.ranger.view.status.StatusFragment
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
		
		setupBottomMenu()
		
		this.saveUserLoginWith()
		
		newReportFabButton.setOnClickListener {
			analytics.trackStartToAddReportEvent()
			ReportActivity.startIntent(this)
		}
		
		if (savedInstanceState == null) {
			menuStatus.performClick()
		}
		
		bottomSheetBehavior = BottomSheetBehavior.from(bottomSheetContainer)
		bottomSheetBehavior.setBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
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
		menuStatus.setOnClickListener {
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
	}
	
	private fun onBottomMenuClick(menu: View) {
		if ((menu as BottomNavigationMenuItem).menuSelected) return
		when (menu.id) {
			menuStatus.id -> {
				menuStatus.menuSelected = true
				menuMap.menuSelected = false
				menuAlert.menuSelected = false
				menuProfile.menuSelected = false
				
				startFragment(StatusFragment.newInstance(), StatusFragment.tag, true)
			}
			menuMap.id -> {
				menuStatus.menuSelected = false
				menuMap.menuSelected = true
				menuAlert.menuSelected = false
				menuProfile.menuSelected = false
				
				startFragment(MapFragment.newInstance(), MapFragment.tag, false)
			}
			menuAlert.id -> {
				menuStatus.menuSelected = false
				menuMap.menuSelected = false
				menuAlert.menuSelected = true
				menuProfile.menuSelected = false
				startFragment(AlertsFragment.newInstance(null, 0), AlertsFragment.tag, true)
			}
			
			menuProfile.id -> {
				menuStatus.menuSelected = false
				menuMap.menuSelected = false
				menuAlert.menuSelected = false
				menuProfile.menuSelected = true
				startFragment(ProfileFragment.newInstance(), ProfileFragment.tag, true)
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
		menuStatus.menuSelected = false
		menuMap.menuSelected = false
		menuAlert.menuSelected = true
		menuProfile.menuSelected = false
		
		startFragment(AlertsFragment.newInstance(null, 1), AlertsFragment.tag, true)
	}
	
	override fun moveMapIntoReportMarker(report: Report) {
		val mapFragment = supportFragmentManager.findFragmentByTag(MapFragment.tag)
		if (mapFragment is MapFragment) {
			mapFragment.moveToReportMarker(report)
		}
	}
	
	private fun startFragment(fragment: Fragment, tag: String = "fragment", showAboveAppbar: Boolean) {
		this.currentFragment = fragment
		val contentContainerPaddingBottom =
				if (showAboveAppbar) resources.getDimensionPixelSize(R.dimen.bottom_bar_height) else 0
		
		contentContainer.setPadding(0, 0, 0, contentContainerPaddingBottom)
		supportFragmentManager.beginTransaction()
				.replace(contentContainer.id, fragment,
						tag).commit()
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
	
	private fun observeEventFromNotification() {
		mainViewModel.eventGuIdFromNotification.observe(this, Observer {
			
			val alertsFragment =
					supportFragmentManager.findFragmentByTag(AlertsFragment.tag)
			if (alertsFragment != null && alertsFragment is AlertsFragment) {
				alertsFragment.showDetail(it, EventItem.State.NONE)
			} else {
				menuStatus.menuSelected = false
				menuMap.menuSelected = false
				menuAlert.menuSelected = true
				menuProfile.menuSelected = false
				startFragment(AlertsFragment.newInstance(it, 1), AlertsFragment.tag, true)
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