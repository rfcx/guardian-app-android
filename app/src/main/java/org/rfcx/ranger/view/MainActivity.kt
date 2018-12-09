package org.rfcx.ranger.view

import android.Manifest
import android.app.Activity
import android.content.*
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.work.WorkInfo
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsResponse
import com.google.android.gms.location.SettingsClient
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
import org.rfcx.ranger.adapter.entity.*
import org.rfcx.ranger.entity.event.Event
import org.rfcx.ranger.entity.message.Message
import org.rfcx.ranger.localdb.LocationDb
import org.rfcx.ranger.localdb.ReportDb
import org.rfcx.ranger.repo.MessageContentProvider
import org.rfcx.ranger.repo.api.ReviewEventApi
import org.rfcx.ranger.service.LocationSyncWorker
import org.rfcx.ranger.service.LocationTrackerService.Companion.locationRequest
import org.rfcx.ranger.service.NetworkReceiver
import org.rfcx.ranger.service.NetworkState
import org.rfcx.ranger.service.ReportSyncWorker
import org.rfcx.ranger.util.*


class MainActivity : AppCompatActivity(), OnMessageItemClickListener, HeaderProtocol,
		OnCompleteListener<Void>,
		EventDialogFragment.OnAlertConfirmCallback,
		OnFailureListener, NetworkReceiver.NetworkStateLister {

	lateinit var messageAdapter: MessageAdapter
	private lateinit var rangerRemote: FirebaseRemoteConfig
	private var isTracking :Boolean = false
	private var networkState :NetworkState = NetworkState.ONLINE
	private var syncInfo :SyncInfo? = null

	// broadcast receiver
	private val onNetworkReceived by lazy { NetworkReceiver(this) }
	
	companion object {
		fun startActivity(context: Context) {
			val intent = Intent(context, MainActivity::class.java)
			context.startActivity(intent)
		}
		
		private const val REQUEST_CODE_REPORT = 201
		private const val REQUEST_CODE_GOOGLE_AVAILABILITY = 100
		private const val REQUEST_PERMISSIONS_REQUEST_CODE = 34
		private const val REQUEST_CHECK_LOCATION_SETTINGS = 35
		const val INTENT_FILTER_MESSAGE_BROADCAST = "${BuildConfig.APPLICATION_ID}.MESSAGE_RECEIVE"
		const val CONNECTIVITY_ACTION = "android.net.conn.CONNECTIVITY_CHANGE"
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

		isTracking = LocationTracking.isOn(this)
		if (isTracking) {
			if (!this.isUsingLocationAllowed()) {
				requestPermissions()
			} else {
				checkLocationIsAllow()
			}
		}

		observeWork()
	}
	
	override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
		super.onActivityResult(requestCode, resultCode, data)
		if (requestCode == REQUEST_CODE_REPORT && resultCode == Activity.RESULT_OK) {
			ReportSuccessDIalogFragment().show(supportFragmentManager, null)

			// TODO: update sync information after save report
			updateSyncInfo()

		} else if (requestCode == REQUEST_CHECK_LOCATION_SETTINGS) {
			if (resultCode == Activity.RESULT_OK) {
				LocationTracking.set(this@MainActivity, true)
			} else {
				LocationTracking.set(this@MainActivity, false)
			}
		}
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
			
			R.id.menu_check_in_history-> DiagnosticsLocationActivity.startIntent(this)
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
		messageAdapter = MessageAdapter(this@MainActivity, this@MainActivity, this@MainActivity)
		messageRecyclerView.setHasFixedSize(true)
		messageRecyclerView.layoutManager = LinearLayoutManager(this@MainActivity)
		messageRecyclerView.adapter = messageAdapter
	}
	
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
		
		MessageContentProvider.getMessageAndEvent(this,
				rangerRemote.getBoolean(RemoteConfigKey.REMOTE_SHOW_EVENT_LIST),
				object : MessageContentProvider.OnContentCallBack {
					override fun onContentLoaded(messages: List<Message>?, events: List<Event>?) {
						
						val baseItems: ArrayList<BaseItem> = ArrayList()
						val recentList = ArrayList<BaseItem>()
						val historyList = ArrayList<BaseItem>()
						
						messages?.let {
							for (message in messages) {
								val localMessage = RealmHelper.getInstance().findLocalMessage(message.guid)
								localMessage?.let {
									message.isOpened = localMessage.isOpened
								}
								if (message.isOpened) {
									historyList.add(MessageItem(message))
								} else {
									recentList.add(MessageItem(message))
								}
							}
						}
						
						events?.let {
							for (event in events) {
								val localEvent = RealmHelper.getInstance().findLocalEvent(event.event_guid)
								localEvent?.let {
									event.isConfirmed = localEvent.isConfirmed
									event.isOpened = localEvent.isOpened
								}
								if (event.isOpened) {
									historyList.add(EventItem(event))
								} else {
									recentList.add(EventItem(event))
								}
							}
						}
						
						recentList.sortWith(compareByDescending {
							when (it) {
								is MessageItem -> DateHelper.getDateTime(it.message.time)
								is EventItem -> DateHelper.getDateTime(it.event.beginsAt)
								else -> {
									0
								}
							}
						})
						
						historyList.sortWith(compareByDescending {
							when (it) {
								is MessageItem -> DateHelper.getDateTime(it.message.time)
								is EventItem -> DateHelper.getDateTime(it.event.beginsAt)
								else -> {
									0
								}
							}
						})

						if (recentList.isNotEmpty()) {
							baseItems.add(TitleItem(getString(R.string.recent_title)))
							baseItems.addAll(recentList)
						}

						if (historyList.isNotEmpty()) {
							baseItems.add(TitleItem(getString(R.string.history_title)))
							baseItems.addAll(historyList)
						}

						if (recentList.isNullOrEmpty() && historyList.isNullOrEmpty()) {
							baseItems.add(EmptyItem())
						}

						messageAdapter.updateMessages(baseItems)
						messageSwipeRefresh.isRefreshing = false
					}
					
					override fun onFailed(t: Throwable?, message: String?) {
						val error: String = if (message.isNullOrEmpty()) getString(R.string.error_common) else message
						Snackbar.make(rootView, error, Snackbar.LENGTH_LONG).show()
					}
				})

		if (LocationDb().unsentCount() > 0) {
			LocationSyncWorker.enqueue()
		}
		if (ReportDb().unsentCount() > 0) {
			ReportSyncWorker.enqueue()
		}
	}
	
	private fun refreshHeader() {
		val preferences = Preferences.getInstance(this)
		val site = preferences.getString(Preferences.DEFAULT_SITE, "")
		val nickname = preferences.getString(Preferences.NICKNAME, "$site Ranger")
		isTracking = LocationTracking.isOn(this)
		messageAdapter.updateHeader(nickname, site, isTracking)
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
	override fun isEnableTracking(): Boolean = this.isTracking
	override fun getNetworkState(): NetworkState = this.networkState
	override fun getSyncInfo(): SyncInfo? = syncInfo

	override fun onPressCancelSync() {
		//TODO: handle on cancel sync reports
	}
	//endregion

	override fun onLocationTrackingChange(on: Boolean) {
		isTracking = on // update tracking
		if (on) {
			if (!isUsingLocationAllowed()) {
				requestPermissions()
			} else {
				checkLocationIsAllow()
			}
		} else {
			LocationTracking.set(this, false)
		}
	}
	
	override fun onCurrentAlert(event: Event) {
		RealmHelper.getInstance().updateConfirmedEvent(event)
		messageAdapter.notifyDataSetChanged()
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
	
	private fun requestPermissions() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
					REQUEST_PERMISSIONS_REQUEST_CODE)
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
				if (!isConfirmEvent) {
					//refresh list if user report reject
					fetchContentList()
				}
			}
			
			override fun onFailed(t: Throwable?, message: String?) {
				Log.d("reportEvent", "onFailed $message")
			}
			
		})
	}
	
	override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>,
	                                        grantResults: IntArray) {
		if (requestCode == REQUEST_PERMISSIONS_REQUEST_CODE) {
			if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
				checkLocationIsAllow()
			} else {
				val shouldProvideRationale = ActivityCompat.shouldShowRequestPermissionRationale(this@MainActivity,
						Manifest.permission.ACCESS_FINE_LOCATION)
				if (!shouldProvideRationale) {
					val dialogBuilder: AlertDialog.Builder =
							AlertDialog.Builder(this@MainActivity).apply {
								setTitle(null)
								setMessage(R.string.location_permission_msg)
								setPositiveButton(R.string.go_to_setting) { _, _ ->
									val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
											Uri.parse("package:$packageName"))
									intent.addCategory(Intent.CATEGORY_DEFAULT)
									intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
									startActivity(intent)
								}
							}
					dialogBuilder.create().show()
				}
			}
		}
	}
	
	/**
	 * Checking phone is turn on location on setting
	 */
	private fun checkLocationIsAllow() {
		val builder = LocationSettingsRequest.Builder()
				.addLocationRequest(locationRequest)
		val client: SettingsClient = LocationServices.getSettingsClient(this)
		val task: Task<LocationSettingsResponse> = client.checkLocationSettings(builder.build())
		task.addOnSuccessListener {
			LocationTracking.set(this@MainActivity, true)
			refreshHeader()
		}
		
		task.addOnFailureListener { exception ->
			if (exception is ResolvableApiException) {
				// Location settings are not satisfied, but this can be fixed
				// by showing the user a dialog.
				LocationTracking.set(this@MainActivity, false)
				try {
					// Show the dialog by calling startResolutionForResult(),
					// and check the result in onActivityResult().
					exception.startResolutionForResult(this@MainActivity,
							REQUEST_CHECK_LOCATION_SETTINGS)
				} catch (sendEx: IntentSender.SendIntentException) {
					// Ignore the error.
					sendEx.printStackTrace()
				}
				
				refreshHeader()
			}
		}
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
					}
					else {
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
