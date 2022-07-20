package org.rfcx.incidents.view

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.location.Location
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.mapbox.android.core.permissions.PermissionsManager
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.rfcx.incidents.R
import org.rfcx.incidents.data.preferences.Preferences
import org.rfcx.incidents.databinding.ActivityMainBinding
import org.rfcx.incidents.entity.CrashlyticsKey
import org.rfcx.incidents.entity.event.Event
import org.rfcx.incidents.entity.location.Coordinate
import org.rfcx.incidents.entity.location.Tracking
import org.rfcx.incidents.entity.response.Response
import org.rfcx.incidents.entity.stream.Stream
import org.rfcx.incidents.service.EventNotification
import org.rfcx.incidents.service.NetworkReceiver
import org.rfcx.incidents.service.NetworkReceiver.Companion.CONNECTIVITY_ACTION
import org.rfcx.incidents.service.NetworkState
import org.rfcx.incidents.service.ResponseSyncWorker
import org.rfcx.incidents.util.Crashlytics
import org.rfcx.incidents.util.LocationPermissions
import org.rfcx.incidents.util.LocationPermissions.Companion.REQUEST_PERMISSIONS_REQUEST_CODE
import org.rfcx.incidents.util.Screen
import org.rfcx.incidents.util.getUserEmail
import org.rfcx.incidents.util.logout
import org.rfcx.incidents.util.saveUserLoginWith
import org.rfcx.incidents.util.setupDisplayTheme
import org.rfcx.incidents.util.startLocationChange
import org.rfcx.incidents.view.base.BaseActivity
import org.rfcx.incidents.view.events.StreamsFragment
import org.rfcx.incidents.view.events.detail.EventActivity
import org.rfcx.incidents.view.events.detail.StreamDetailFragment
import org.rfcx.incidents.view.profile.ProfileFragment
import org.rfcx.incidents.view.profile.ProfileViewModel.Companion.DOWNLOADING_STATE
import org.rfcx.incidents.view.profile.ProfileViewModel.Companion.DOWNLOAD_CANCEL_STATE
import org.rfcx.incidents.view.report.create.CreateReportActivity
import org.rfcx.incidents.view.report.create.CreateReportActivity.Companion.RESULT_CODE
import org.rfcx.incidents.view.report.detail.ResponseDetailActivity
import org.rfcx.incidents.view.report.draft.DraftReportsFragment
import org.rfcx.incidents.view.report.submitted.SubmittedReportsFragment
import org.rfcx.incidents.widget.BottomNavigationMenuItem

class MainActivity : BaseActivity(), MainActivityEventListener, NetworkReceiver.NetworkStateLister {
    private lateinit var binding: ActivityMainBinding
    private val mainViewModel: MainActivityViewModel by viewModel()
    private val preferences = Preferences.getInstance(this)
    private val firebaseCrashlytics by lazy { Crashlytics() }

    private val locationPermissions by lazy { LocationPermissions(this) }
    private val onNetworkReceived by lazy { NetworkReceiver(this) }
    private var currentFragment: Fragment? = null
    private var currentLocation: Location? = null

    private val getResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (it.resultCode == RESULT_CODE) {
            showBottomAppBar()
            supportFragmentManager.popBackStack()

            when (it.data?.getStringExtra(CreateReportActivity.EXTRA_SCREEN)) {
                Screen.DRAFT_REPORTS.id -> binding.navMenu.menuDraftReports.performClick()
                Screen.SUBMITTED_REPORTS.id -> binding.navMenu.menuSubmittedReports.performClick()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        setupDisplayTheme()
        setStatusBar()
        firebaseCrashlytics.setCustomKey(CrashlyticsKey.EmailUser.key, this.getUserEmail())
        if (supportFragmentManager.fragments.lastOrNull() is StreamDetailFragment) hideBottomAppBar()

        // TODO: move preferences to viewmodel
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
        getEventFromIntentIfHave(intent)
        this.startLocationChange()
    }

    override fun onResume() {
        registerReceiver(onNetworkReceived, IntentFilter(CONNECTIVITY_ACTION))
        super.onResume()
    }

    override fun onPause() {
        unregisterReceiver(onNetworkReceived)
        super.onPause()
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        getEventFromIntentIfHave(intent)
    }

    override fun onBackPressed() {
        when (supportFragmentManager.findFragmentById(R.id.contentContainer)) {
            is StreamDetailFragment -> {
                if (supportFragmentManager.backStackEntryCount > 0) {
                    supportFragmentManager.popBackStack()
                } else {
                    super.onBackPressed()
                }
                showBottomAppBar()
            }
            else -> {
                return super.onBackPressed()
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        locationPermissions.handleRequestResult(requestCode, grantResults)

        if (supportFragmentManager.fragments.lastOrNull() is StreamDetailFragment) {
            if (requestCode == REQUEST_PERMISSIONS_REQUEST_CODE) {
                getCurrentLocation()?.let { loc ->
                    saveLocation(loc)
                }
                openCreateReportActivity(Preferences.getInstance(this).getString(Preferences.SELECTED_STREAM_ID, ""))
            }
        }

        currentFragment?.let {
            if (it is StreamsFragment) {
                it.onRequestPermissionsResult(requestCode, permissions, grantResults)
                if (PermissionsManager.areLocationPermissionsGranted(this)) {
                    this.startLocationChange()
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        locationPermissions.handleActivityResult(requestCode, resultCode)

        currentFragment?.let {
            if (it is StreamsFragment) {
                it.onActivityResult(requestCode, resultCode, data)
            }
        }
    }

    private fun setStatusBar() {
        val window = this.window
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.statusBarColor = ContextCompat.getColor(this, R.color.statusColor)
        }
    }

    private fun saveLocation(location: Location) {
        val tracking = Tracking(id = 1)
        val coordinate = Coordinate(
            latitude = location.latitude,
            longitude = location.longitude,
            altitude = location.altitude
        )
        mainViewModel.saveLocation(tracking, coordinate)
        Preferences.getInstance(this).putLong(Preferences.LATEST_GET_LOCATION_TIME, System.currentTimeMillis())
    }

    private fun setupBottomMenu() {
        binding.navMenu.menuNewEvents.setOnClickListener {
            onBottomMenuClick(it)
        }
        binding.navMenu.menuSubmittedReports.setOnClickListener {
            onBottomMenuClick(it)
        }
        binding.navMenu.menuDraftReports.setOnClickListener {
            onBottomMenuClick(it)
        }
        binding.navMenu.menuProfile.setOnClickListener {
            onBottomMenuClick(it)
        }
    }

    private fun onBottomMenuClick(menu: View) {
        if ((menu as BottomNavigationMenuItem).menuSelected) return
        when (menu.id) {
            binding.navMenu.menuNewEvents.id -> {
                binding.navMenu.menuNewEvents.menuSelected = true
                binding.navMenu.menuSubmittedReports.menuSelected = false
                binding.navMenu.menuDraftReports.menuSelected = false
                binding.navMenu.menuProfile.menuSelected = false

                showStatus()
            }
            binding.navMenu.menuSubmittedReports.id -> {
                binding.navMenu.menuNewEvents.menuSelected = false
                binding.navMenu.menuSubmittedReports.menuSelected = true
                binding.navMenu.menuDraftReports.menuSelected = false
                binding.navMenu.menuProfile.menuSelected = false

                showSubmittedReports()
            }
            binding.navMenu.menuDraftReports.id -> {
                binding.navMenu.menuNewEvents.menuSelected = false
                binding.navMenu.menuSubmittedReports.menuSelected = false
                binding.navMenu.menuDraftReports.menuSelected = true
                binding.navMenu.menuProfile.menuSelected = false

                showDraftReports()
            }

            binding.navMenu.menuProfile.id -> {
                binding.navMenu.menuNewEvents.menuSelected = false
                binding.navMenu.menuSubmittedReports.menuSelected = false
                binding.navMenu.menuDraftReports.menuSelected = false
                binding.navMenu.menuProfile.menuSelected = true

                showProfile()
            }
        }
    }

    override fun showBottomSheet(fragment: Fragment) {}

    override fun hideBottomSheet() {}

    override fun hideBottomAppBar() {
        showAboveAppbar(false)
        binding.bottomBar.visibility = View.GONE
    }

    override fun showBottomAppBar() {
        showAboveAppbar(true)
        binding.bottomBar.visibility = View.VISIBLE
    }

    override fun openStreamDetail(streamId: String, distance: Double?) {
        hideBottomAppBar()
        startFragment(
            StreamDetailFragment.newInstance(streamId, distance),
            StreamDetailFragment.tag
        )
    }

    private fun startFragment(fragment: Fragment, tag: String = "") {
        if (tag.isBlank()) {
            supportFragmentManager.beginTransaction()
                .replace(binding.contentContainer.id, fragment)
                .commit()
        } else {
            supportFragmentManager.beginTransaction()
                .replace(binding.contentContainer.id, fragment, tag)
                .addToBackStack(tag)
                .commit()
        }
    }

    override fun openCreateReportActivity(streamId: String) {
        val intent = Intent(this, CreateReportActivity::class.java)
        intent.putExtra(CreateReportActivity.EXTRA_STREAM_ID, streamId)
        getResult.launch(intent)
    }

    override fun openDetailResponse(coreId: String) {
        ResponseDetailActivity.startActivity(this, coreId)
    }

    override fun openCreateResponse(response: Response) {
        val intent = Intent(this, CreateReportActivity::class.java)
        intent.putExtra(CreateReportActivity.EXTRA_RESPONSE_ID, response.id)
        intent.putExtra(CreateReportActivity.EXTRA_STREAM_ID, response.streamId)
        getResult.launch(intent)
    }

    override fun openGoogleMap(stream: Stream) {
        val intent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("geo:${stream.latitude},${stream.longitude}?q=${stream.latitude},${stream.longitude}(${stream.name})")
        )
        startActivity(intent)
    }

    override fun openEvent(event: Event) {
        EventActivity.startActivity(this, event.id)
    }

    override fun setCurrentLocation(location: Location) {
        currentLocation = location
    }

    override fun getCurrentLocation(): Location? {
        return currentLocation
    }

    private fun setupFragments() {
        supportFragmentManager.beginTransaction()
            .add(binding.contentContainer.id, getProfile(), ProfileFragment.tag)
            .add(binding.contentContainer.id, getSubmittedReports(), SubmittedReportsFragment.tag)
            .add(binding.contentContainer.id, getDraftReports(), DraftReportsFragment.tag)
            .add(binding.contentContainer.id, getNewEvents(), StreamsFragment.tag)
            .commit()

        binding.navMenu.menuNewEvents.performClick()
    }

    private fun getNewEvents(): StreamsFragment = supportFragmentManager.findFragmentByTag(StreamsFragment.tag)
        as StreamsFragment? ?: StreamsFragment.newInstance()

    private fun getSubmittedReports(): SubmittedReportsFragment =
        supportFragmentManager.findFragmentByTag(SubmittedReportsFragment.tag)
            as SubmittedReportsFragment? ?: SubmittedReportsFragment.newInstance()

    private fun getDraftReports(): DraftReportsFragment =
        supportFragmentManager.findFragmentByTag(DraftReportsFragment.tag)
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
        binding.contentContainer.setPadding(0, 0, 0, contentContainerPaddingBottom)
    }

    private fun observeMain() {
        mainViewModel.isRequireToLogin.observe(
            this,
            Observer {
                if (it) logout()
            }
        )
    }

    private fun getEventFromIntentIfHave(intent: Intent?) {
        if (intent?.hasExtra(EventNotification.INTENT_KEY_STREAM_ID) == true) {
            val dialog = Dialog(this)
            dialog.setContentView(R.layout.fragment_loading)
            dialog.setCancelable(false)
            dialog.show()

            val streamId = intent.getStringExtra(EventNotification.INTENT_KEY_STREAM_ID)
            if (streamId == null) {
                dialog.dismiss()
                return
            }

            if (mainViewModel.getStream(streamId) != null) {
                openStreamDetail(streamId, null)
                dialog.dismiss()
            }

            getSubscribedProject().forEach { id ->
                mainViewModel.refreshStreams(id) { streams ->
                    if (streams.isNullOrEmpty()) return@refreshStreams
                    if (streams.any { s -> s.id == streamId }) {
                        openStreamDetail(streamId, null)
                        dialog.dismiss()
                    }
                }
            }
        }
    }

    private fun getSubscribedProject(): List<String> {
        return preferences.getArrayList(Preferences.SUBSCRIBED_PROJECTS) ?: listOf()
    }

    companion object {
        fun startActivity(context: Context, streamId: String?) {
            val intent = Intent(context, MainActivity::class.java)
            if (streamId != null)
                intent.putExtra(EventNotification.INTENT_KEY_STREAM_ID, streamId)
            context.startActivity(intent)
        }
    }

    override fun onNetworkStateChange(state: NetworkState) {
        if (state == NetworkState.ONLINE) {
            ResponseSyncWorker.enqueue()
        }
    }
}

interface MainActivityEventListener {
    fun showBottomSheet(fragment: Fragment)
    fun hideBottomSheet()
    fun hideBottomAppBar()
    fun showBottomAppBar()
    fun onBackPressed()
    fun openStreamDetail(name: String, distance: Double?)
    fun openCreateReportActivity(streamId: String)
    fun openDetailResponse(coreId: String)
    fun openCreateResponse(response: Response)
    fun openGoogleMap(stream: Stream)
    fun openEvent(event: Event)
    fun setCurrentLocation(location: Location)
    fun getCurrentLocation(): Location?
}
