package org.rfcx.ranger.view

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.work.WorkInfo
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.Task
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import kotlinx.android.synthetic.main.activity_message_list.*
import org.rfcx.ranger.BuildConfig
import org.rfcx.ranger.R
import org.rfcx.ranger.adapter.HeaderProtocol
import org.rfcx.ranger.adapter.MessageAdapter
import org.rfcx.ranger.adapter.OnMessageItemClickListener
import org.rfcx.ranger.adapter.SyncInfo
import org.rfcx.ranger.adapter.entity.BaseItem
import org.rfcx.ranger.adapter.entity.EventItem
import org.rfcx.ranger.adapter.entity.MessageItem
import org.rfcx.ranger.entity.event.Event
import org.rfcx.ranger.localdb.LocationDb
import org.rfcx.ranger.localdb.ReportDb
import org.rfcx.ranger.repo.MessageContentProvider
import org.rfcx.ranger.repo.api.ReviewEventApi
import org.rfcx.ranger.service.LocationSyncWorker
import org.rfcx.ranger.service.NetworkReceiver
import org.rfcx.ranger.service.NetworkState
import org.rfcx.ranger.service.ReportSyncWorker
import org.rfcx.ranger.util.*
import kotlin.math.ceil

class MainActivity : AppCompatActivity(), OnMessageItemClickListener, HeaderProtocol,
		OnCompleteListener<Void>,
		EventDialogFragment.OnAlertConfirmCallback,
		OnFailureListener, NetworkReceiver.NetworkStateLister {

	lateinit var messageAdapter: MessageAdapter
	private lateinit var rangerRemote: FirebaseRemoteConfig
	private var networkState: NetworkState = NetworkState.ONLINE
	private var syncInfo: SyncInfo? = null

	private val onNetworkReceived by lazy { NetworkReceiver(this) }
	private val locationPermissions by lazy { LocationPermissions(this) }

    // pagination
    private var isLoading: Boolean = false
	private var currentOffset: Int = 0
	private var totalItemCount: Int = 0

	private val totalPage: Int
		get() = ceil(totalItemCount.toFloat() / LIMIT_PER_PAGE).toInt()

	private val nextOffset: Int
		get() {
			currentOffset += LIMIT_PER_PAGE
			return currentOffset
		}

	companion object {
		fun startActivity(context: Context) {
			val intent = Intent(context, MainActivity::class.java)
			context.startActivity(intent)
		}

		private const val REQUEST_CODE_REPORT = 201
		private const val REQUEST_CODE_GOOGLE_AVAILABILITY = 100
		const val INTENT_FILTER_MESSAGE_BROADCAST = "${BuildConfig.APPLICATION_ID}.MESSAGE_RECEIVE"
		const val CONNECTIVITY_ACTION = "android.net.conn.CONNECTIVITY_CHANGE"
		private const val LIMIT_PER_PAGE = 12
	}

	override fun onStart() {
		super.onStart()
		checkGoogleApiAvailability()
	}

	override fun onNewIntent(intent: Intent?) {
		super.onNewIntent(intent)
		// the activity open from another task eg. bypass from @LoginActivity by click notification -> reload the list.
		fetchContentList()
	}

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_message_list)

		initToolbar()
		initAdapter()
		initRemoteConfig()
		fetchRangerRemoteConfig()
		messageSwipeRefresh.setOnRefreshListener {
			fetchRangerRemoteConfig()
		}
		fab.setOnClickListener {
			startActivityForResult(Intent(this@MainActivity, ReportActivity::class.java),
					REQUEST_CODE_REPORT)
		}

		messageRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
			override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
				if (dy > 0)
					fab.hide()
				else if (dy < 0)
					fab.show()

				super.onScrolled(recyclerView, dx, dy)
			}
		})

		onLocationTrackingChange(LocationTracking.isOn(this))

		observeWork()
	}

	override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
		super.onActivityResult(requestCode, resultCode, data)

		if (requestCode == REQUEST_CODE_REPORT && resultCode == Activity.RESULT_OK) {
			ReportSuccessDialogFragment().show(supportFragmentManager, null)
			updateSyncInfo()
		}

		locationPermissions.handleActivityResult(requestCode, resultCode)
	}

	override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
		locationPermissions.handleRequestResult(requestCode, grantResults)
	}

	private fun updateSyncInfo(status: SyncInfo.Status? = null) {
		val syncStatus = status ?: when (networkState) {
			NetworkState.OFFLINE -> SyncInfo.Status.WAITING_NETWORK
			NetworkState.ONLINE -> SyncInfo.Status.STARTING
		}

		val locationCount = LocationDb().unsentCount()
		val reportCount = ReportDb().unsentCount()
		syncInfo = SyncInfo(syncStatus, reportCount, locationCount)

		refreshHeader()
	}

	override fun onResume() {
		super.onResume()
		CloudMessaging.subscribeIfRequired(this)
		// register BroadcastReceiver
		registerReceiver(onEventNotificationReceived, IntentFilter(INTENT_FILTER_MESSAGE_BROADCAST))
		registerReceiver(onNetworkReceived, IntentFilter(CONNECTIVITY_ACTION))

		refreshHeader()
	}

	override fun onPause() {
		super.onPause()
		unregisterReceiver(onEventNotificationReceived)
		unregisterReceiver(onNetworkReceived)
	}

	override fun onCreateOptionsMenu(menu: Menu?): Boolean {
		val inflater = menuInflater
		inflater.inflate(R.menu.ranger_menu, menu)
		return super.onCreateOptionsMenu(menu)
	}

	override fun onOptionsItemSelected(item: MenuItem?): Boolean {
		when (item?.itemId) {
			R.id.menu_logout -> {
				logout()
			}
			R.id.menu_settings -> {
				startActivity(Intent(this@MainActivity, SettingsActivity::class.java))
			}

			R.id.menu_check_in_history -> DiagnosticsLocationActivity.startIntent(this)
		}
		return super.onOptionsItemSelected(item)
	}

	//region {@link NetworkReceiver.NetworkStateLister} implementation
	override fun onNetworkStateChange(state: NetworkState) {
		this.networkState = state

		updateSyncInfo()

		if (state == NetworkState.ONLINE) {
			fetchContentList()
		}
	}
	//endregion

	//region {@link addOnCompleteListener.onComplete} implementation
	override fun onComplete(task: Task<Void>) {
		if (task.isSuccessful) {
			Log.d(this@MainActivity.localClassName, "Fetch remote successful!")
			rangerRemote.activateFetched() // active config
			Log.d("Ranger remote", RemoteConfigKey.REMOTE_NOTI_FREQUENCY_DURATION + ": " + rangerRemote.getString(RemoteConfigKey.REMOTE_NOTI_FREQUENCY_DURATION))
			Log.d("Ranger remote", RemoteConfigKey.REMOTE_ENABLE_NOTI_MESSAGE + ": " + rangerRemote.getString(RemoteConfigKey.REMOTE_ENABLE_NOTI_MESSAGE))
			Log.d("Ranger remote", RemoteConfigKey.REMOTE_SHOW_EVENT_LIST + ": " + rangerRemote.getString(RemoteConfigKey.REMOTE_SHOW_EVENT_LIST))
			Log.d("Ranger remote", RemoteConfigKey.REMOTE_ENABLE_NOTI_EVENT_ALERT + ": " + rangerRemote.getString(RemoteConfigKey.REMOTE_ENABLE_NOTI_EVENT_ALERT))
		}
		fetchContentList()
	}

	override fun onFailure(e: java.lang.Exception) {
		e.printStackTrace()
	}
	// end region

	private fun initToolbar() {
		setSupportActionBar(messageToolbar)
		supportActionBar?.setDisplayShowTitleEnabled(false)
	}

	private fun initAdapter() {
		val layoutManager = LinearLayoutManager(this@MainActivity)
		messageAdapter = MessageAdapter(this@MainActivity, this@MainActivity, this@MainActivity)
		messageRecyclerView.setHasFixedSize(true)
		messageRecyclerView.layoutManager = layoutManager
		messageRecyclerView.addOnScrollListener(getScrollLoadMore(layoutManager)) // add custom scrolling for load more
		messageRecyclerView.adapter = messageAdapter
	}

    private fun getScrollLoadMore(layoutManager: LinearLayoutManager) = object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            val visibleItemCount = layoutManager.childCount
            val total = layoutManager.itemCount
            val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()
			if (!isLoading && !isLastPage()) {
				if ((visibleItemCount + firstVisibleItemPosition) >= total
						&& firstVisibleItemPosition >= 0
						&& total >= LIMIT_PER_PAGE) {
					loadMoreEvents()
				}
			}
        }
    }

	private fun isLastPage() : Boolean = currentOffset >= totalPage

	private fun initRemoteConfig() {
		rangerRemote = FirebaseRemoteConfig.getInstance()

		// config for debug
		val configSettings = FirebaseRemoteConfigSettings.Builder()
				.setDeveloperModeEnabled(BuildConfig.DEBUG)
				.build()

		rangerRemote.setConfigSettings(configSettings)
		rangerRemote.setDefaults(R.xml.ranger_remote_config_defualt)
	}

	private fun fetchRangerRemoteConfig() {
		Log.d(this@MainActivity.packageName, "Start fetch remote config!")
		// cache config
		var cacheExpiration: Long = 3600 // 1 hour
		if (rangerRemote.info.configSettings.isDeveloperModeEnabled) {
			cacheExpiration = 0
		}
		rangerRemote.fetch(cacheExpiration).addOnCompleteListener(this)
				.addOnFailureListener(this@MainActivity)
	}

	private fun fetchContentList() {
		messageSwipeRefresh.isRefreshing = true

		// reset offset
		currentOffset = 0

		MessageContentProvider.getEvents(this, LIMIT_PER_PAGE, currentOffset,
				object : MessageContentProvider.OnEventsCallback {
			override fun onEventsLoaded(events: List<Event>, totalItemCount: Int) {
				this@MainActivity.totalItemCount = totalItemCount // update total count
				messageAdapter.updateEvents(events)
				messageSwipeRefresh.isRefreshing = false
			}

			override fun onFailed(t: Throwable?, message: String?) {
				val error: String = if (message.isNullOrEmpty()) getString(R.string.error_common) else message
				Snackbar.make(rootView, error, Snackbar.LENGTH_LONG).show()
				messageSwipeRefresh.isRefreshing = false
			}
		})

		if (LocationDb().unsentCount() > 0) {
			LocationSyncWorker.enqueue()
		}
		if (ReportDb().unsentCount() > 0) {
			ReportSyncWorker.enqueue()
		}
	}

	private fun loadMoreEvents() {
		Log.d(this@MainActivity.localClassName, "load more...")
		isLoading = true
		messageSwipeRefresh.isRefreshing = true

		MessageContentProvider.getEvents(this, LIMIT_PER_PAGE, nextOffset,
				object : MessageContentProvider.OnEventsCallback {
					override fun onEventsLoaded(events: List<Event>, totalItemCount: Int) {
						this@MainActivity.totalItemCount = totalItemCount // update total count
						messageAdapter.addEventsFromLoadMore(events)
						messageSwipeRefresh.isRefreshing = false
						isLoading = false
					}

					override fun onFailed(t: Throwable?, message: String?) {
						// cancel next offset
						currentOffset -= LIMIT_PER_PAGE

						val error: String = if (message.isNullOrEmpty()) getString(R.string.error_common) else message
						Snackbar.make(rootView, error, Snackbar.LENGTH_LONG).show()
						messageSwipeRefresh.isRefreshing = false
						isLoading = false
					}
				})
	}

	private fun refreshHeader() {
		messageAdapter.updateHeader(getUserNickname(), getSiteName(), LocationTracking.isOn(this))
	}

	private fun logout() {
		CloudMessaging.unsubscribe(this)
		Preferences.getInstance(this@MainActivity).clear()
		LoginActivity.startActivity(this@MainActivity)
		finish()
	}

	override fun onMessageItemClick(position: Int) {
		val item: BaseItem? = messageAdapter.getItemAt(position)
		if (item is MessageItem) {
			item.message.let {
				val intent = Intent(android.content.Intent.ACTION_VIEW,
						Uri.parse("http://maps.google.com/maps?q="
								+ it.coords?.lat + ","
								+ it.coords?.lon))
				startActivity(intent)
				RealmHelper.getInstance().updateOpenedMessage(it)
			}
		} else if (item is EventItem) {
			RealmHelper.getInstance().updateOpenedEvent(item.event)
			showAlertPopup(item.event)
		}

		messageAdapter.notifyDataSetChanged()
	}

	//region {@link OnLocationTrackingChangeListener }
	override fun isEnableTracking(): Boolean = LocationTracking.isOn(this)

	override fun getNetworkState(): NetworkState = networkState
	override fun getSyncInfo(): SyncInfo? = syncInfo

	override fun onPressCancelSync() {
		//TODO: handle on cancel sync reports
	}
	//endregion

	override fun onLocationTrackingChange(on: Boolean) {
		if (on) {
			locationPermissions.check { isAllowed: Boolean ->
				LocationTracking.set(this, isAllowed)
			}
		} else {
			LocationTracking.set(this, false)
		}
	}

	override fun onCurrentAlert(event: Event) {
		reportEvent(event, true)
	}

	override fun onIncorrectAlert(event: Event) {
		reportEvent(event, false)
	}

	private fun checkGoogleApiAvailability() {
		val googleApiAvailability = GoogleApiAvailability.getInstance()
		val statusCode = googleApiAvailability.isGooglePlayServicesAvailable(this)
		val isResultSuccess = statusCode == ConnectionResult.SUCCESS
		val isErrorCanBeRecover = googleApiAvailability.isUserResolvableError(statusCode)

		if (!isResultSuccess && isErrorCanBeRecover) {
			googleApiAvailability.showErrorDialogFragment(this, statusCode, REQUEST_CODE_GOOGLE_AVAILABILITY)
		}
	}

	private fun showAlertPopup(event: Event) {
		RealmHelper.getInstance().updateOpenedEvent(event)
		EventDialogFragment.newInstance(event).show(supportFragmentManager, null)
	}

	private fun reportEvent(event: Event, isConfirmEvent: Boolean) {
		ReviewEventApi().reViewEvent(this@MainActivity, event,
				isConfirmEvent, object : ReviewEventApi.ReviewEventCallback {
			override fun onSuccess() {
				fetchContentList()
			}

			override fun onFailed(t: Throwable?, message: String?) {
				val error: String = if (message.isNullOrEmpty()) getString(R.string.error_common) else message
				Snackbar.make(rootView, error, Snackbar.LENGTH_LONG).show()
			}
		})
	}


	/**
	 * BroadcastReceiver when receive message from FireBase Cloud Messaging @MyFireBaseMessagingService
	 * Do -> reload list
	 */
	private val onEventNotificationReceived = object : BroadcastReceiver() {
		override fun onReceive(context: Context?, intent: Intent?) {
			if (intent?.action == INTENT_FILTER_MESSAGE_BROADCAST) {
				fetchContentList()
			}
		}
	}

	private fun observeWork() {
		ReportSyncWorker.workInfos().observe(this,
				Observer<List<WorkInfo>> { workStatusList ->
					val currentWorkStatus = workStatusList?.getOrNull(0)
					if (currentWorkStatus != null) {
						Log.d("MainActivity", "ReportSyncWorker: ${currentWorkStatus.state.name}")
						when (currentWorkStatus.state) {
							WorkInfo.State.RUNNING -> {
								updateSyncInfo(SyncInfo.Status.UPLOADING)
							}
							WorkInfo.State.SUCCEEDED -> {
								updateSyncInfo(SyncInfo.Status.UPLOADED)
							}
							else -> {
								updateSyncInfo()
							}
						}
					} else {
						Log.d("MessageListActivity", "ReportSyncWorker: NO WORK STATUS")
						updateSyncInfo()
					}
				})

		LocationSyncWorker.workInfos().observe(this,
				Observer<List<WorkInfo>> { workStatusList ->
					val currentWorkStatus = workStatusList?.getOrNull(0)
					if (currentWorkStatus != null) {
						Log.d("MainActivity", "LocationSyncWorker: ${currentWorkStatus.state.name}")
						updateSyncInfo()
					}
				})
	}
}
