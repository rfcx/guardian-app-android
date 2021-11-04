package org.rfcx.ranger.view

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_new_events.*
import kotlinx.android.synthetic.main.layout_bottom_navigation_menu.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.rfcx.ranger.R
import org.rfcx.ranger.entity.Stream
import org.rfcx.ranger.entity.report.Report
import org.rfcx.ranger.entity.response.Response
import org.rfcx.ranger.service.*
import org.rfcx.ranger.service.NetworkReceiver.Companion.CONNECTIVITY_ACTION
import org.rfcx.ranger.util.*
import org.rfcx.ranger.view.base.BaseActivity
import org.rfcx.ranger.view.events.EventsFragment
import org.rfcx.ranger.view.events.detail.GuardianEventDetailFragment
import org.rfcx.ranger.view.map.MapFragment
import org.rfcx.ranger.view.profile.ProfileFragment
import org.rfcx.ranger.view.profile.ProfileViewModel.Companion.DOWNLOADING_STATE
import org.rfcx.ranger.view.profile.ProfileViewModel.Companion.DOWNLOAD_CANCEL_STATE
import org.rfcx.ranger.view.report.create.CreateReportActivity
import org.rfcx.ranger.view.report.create.CreateReportActivity.Companion.RESULT_CODE
import org.rfcx.ranger.view.report.draft.DraftReportsFragment
import org.rfcx.ranger.view.report.submitted.SubmittedReportsFragment
import org.rfcx.ranger.widget.BottomNavigationMenuItem


class MainActivity : BaseActivity(), MainActivityEventListener, NetworkReceiver.NetworkStateLister {
	private val locationTrackingViewModel: LocationTrackingViewModel by viewModel()
	private val mainViewModel: MainActivityViewModel by viewModel()
	
	private val locationPermissions by lazy { LocationPermissions(this) }
	private val onNetworkReceived by lazy { NetworkReceiver(this) }
	private var currentFragment: Fragment? = null
	
	private val onAirplaneModeCallback: (Boolean) -> Unit = { isOnAirplaneMode ->
		if (isOnAirplaneMode) {
			LocationTracking.set(this, false)
			locationTrackingViewModel.trackingStateChange()
		}
	}
	private val airplaneModeReceiver = AirplaneModeReceiver(onAirplaneModeCallback)
	
	private val getResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
		if (it.resultCode == RESULT_CODE) {
			showBottomAppBar()
			supportFragmentManager.popBackStack()
			
			when (it.data?.getStringExtra(CreateReportActivity.EXTRA_SCREEN)) {
				Screen.DRAFT_REPORTS.id -> menuDraftReports.performClick()
				Screen.SUBMITTED_REPORTS.id -> menuSubmittedReports.performClick()
			}
		}
	}
	
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_main)
		
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
		registerReceiver(onNetworkReceived, IntentFilter(CONNECTIVITY_ACTION))
		super.onResume()
		mainViewModel.updateLocationTracking()
	}
	
	override fun onPause() {
		unregisterReceiver(airplaneModeReceiver)
		unregisterReceiver(onNetworkReceived)
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
				
				showSubmittedReports()
			}
			menuDraftReports.id -> {
				menuNewEvents.menuSelected = false
				menuSubmittedReports.menuSelected = false
				menuDraftReports.menuSelected = true
				menuProfile.menuSelected = false
				
				showDraftReports()
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
	
	override fun openGuardianEventDetail(name: String, distance: Double?, guardianId: String) {
		hideBottomAppBar()
		startFragment(GuardianEventDetailFragment.newInstance(name, distance, guardianId), GuardianEventDetailFragment.tag)
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
	
	override fun openCreateReportActivity(guardianName: String, guardianId: String) {
		val intent = Intent(this, CreateReportActivity::class.java)
		intent.putExtra(CreateReportActivity.EXTRA_GUARDIAN_NAME, guardianName)
		intent.putExtra(CreateReportActivity.EXTRA_GUARDIAN_ID, guardianId)
		getResult.launch(intent)
	}
	
	override fun openDetailResponse(response: Response) {
		val intent = Intent(this, CreateReportActivity::class.java)
		intent.putExtra(CreateReportActivity.EXTRA_RESPONSE_ID, response.id)
		getResult.launch(intent)
	}
	
	override fun openGoogleMap(stream: Stream) {
		val intent = Intent(Intent.ACTION_VIEW, Uri.parse("geo:${stream.latitude},${stream.longitude}?q=${stream.latitude},${stream.longitude}(${stream.name})"))
		startActivity(intent)
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
	
	private fun showDraftReports() {
		showAboveAppbar(true)
		this.currentFragment = getDraftReports()
		supportFragmentManager.beginTransaction()
				.show(getDraftReports())
				.hide(getNewEvents())
				.hide(getSubmittedReports())
				.hide(getProfile())
				.commit()
	}
	
	private fun showSubmittedReports() {
		showAboveAppbar(true)
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
			val streamName: String? = intent.getStringExtra(AlertNotification.ALERT_ID_NOTI_INTENT)
			streamName?.let { name ->
				val stream = mainViewModel.getStreamByName(name)
				stream?.serverId?.let { openGuardianEventDetail(name, null, it) }
			}
		}
	}
	
	private fun enableLocationTracking() {
		if (isOnAirplaneMode()) {
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
		fun startActivity(context: Context, eventGuId: String?) {
			val intent = Intent(context, MainActivity::class.java)
			if (eventGuId != null)
				intent.putExtra(AlertNotification.ALERT_ID_NOTI_INTENT, eventGuId)
			context.startActivity(intent)
		}
	}
	
	override fun onNetworkStateChange(state: NetworkState) {
		when (state) {
			NetworkState.ONLINE -> ResponseSyncWorker.enqueue()
		}
	}
}

interface MainActivityEventListener {
	fun showBottomSheet(fragment: Fragment)
	fun hideBottomSheet()
	fun hideBottomAppBar()
	fun showBottomAppBar()
	fun onBackPressed()
	fun openGuardianEventDetail(name: String, distance: Double?, guardianId: String)
	fun moveMapIntoReportMarker(report: Report)
	fun openCreateReportActivity(guardianName: String, guardianId: String)
	fun openDetailResponse(response: Response)
	fun openGoogleMap(stream: Stream)
}
