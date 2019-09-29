package org.rfcx.ranger.view

import android.content.Context
import android.content.Intent
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
import org.rfcx.ranger.entity.event.Event
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
	private lateinit var currentFragment: Fragment
	
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_main_new)
		
		setupBottomMenu()
		
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
	
	override fun onStart() {
		super.onStart()
		mainViewModel.getEventAndPreloadAudio()
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
		
		if (currentFragment is MapFragment) {
			(currentFragment as MapFragment).onRequestPermissionsResult(requestCode, permissions, grantResults)
		}
	}
	
	override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
		super.onActivityResult(requestCode, resultCode, data)
		locationPermissions.handleActivityResult(requestCode, resultCode)
		
		if (currentFragment is MapFragment) {
			(currentFragment as MapFragment).onActivityResult(requestCode, resultCode, data)
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
				startFragment(AlertsFragment.newInstance(null), AlertsFragment.tag, true)
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
		onBottomMenuClick(menuAlert)
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
		mainViewModel.eventFromNotification.observe(this, Observer {
			
			val alertsFragment =
					supportFragmentManager.findFragmentByTag(AlertsFragment.tag)
			if (alertsFragment != null && alertsFragment is AlertsFragment) {
				alertsFragment.showDetail(it)
			} else {
				menuStatus.menuSelected = false
				menuMap.menuSelected = false
				menuAlert.menuSelected = true
				menuProfile.menuSelected = false
				startFragment(AlertsFragment.newInstance(it), AlertsFragment.tag, true)
			}
			
		})
	}
	
	private fun getEventFromIntentIfHave(intent: Intent?) {
		if (intent?.hasExtra(AlertNotification.ALERT_NOTI_INTENT) == true) {
			val event = intent.getParcelableExtra<Event>(AlertNotification.ALERT_NOTI_INTENT)
			mainViewModel.eventFromNotification.value = event
		}
	}
	
	private fun enableLocationTracking() {
		if (isOnAirplaneMode()) {
			AlertDialog.Builder(this)
					.setTitle(R.string.in_air_plane_mode)
					.setMessage(R.string.pls_off_air_plane_mode)
					.setPositiveButton(R.string.common_ok, null)
					.show()
			LocationTracking.set(this, false)
			locationTrackingViewModel.trackingStateChange()
		} else {
			locationPermissions.check { hasPermission: Boolean ->
				LocationTracking.set(this, hasPermission)
				locationTrackingViewModel.trackingStateChange()
			}
		}
	}
	
	private fun disableLocationTracking() {
		LocationTracking.set(this, false)
		locationTrackingViewModel.trackingStateChange()
	}
	
	companion object {
		fun startActivity(context: Context, event: Event?) {
			val intent = Intent(context, MainActivityNew::class.java)
			if (event != null)
				intent.putExtra(AlertNotification.ALERT_NOTI_INTENT, event)
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
}

interface MainActivityListener {
	fun alertScreen()
}