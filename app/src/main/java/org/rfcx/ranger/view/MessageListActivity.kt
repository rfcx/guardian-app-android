package org.rfcx.ranger.view

import android.Manifest
import android.content.Context
import android.content.Intent
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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
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
import org.rfcx.ranger.adapter.MessageAdapter
import org.rfcx.ranger.adapter.OnMessageItemClickListener
import org.rfcx.ranger.adapter.entity.BaseItem
import org.rfcx.ranger.adapter.entity.EventItem
import org.rfcx.ranger.adapter.entity.MessageItem
import org.rfcx.ranger.entity.EventResponse
import org.rfcx.ranger.entity.event.Event
import org.rfcx.ranger.entity.message.Message
import org.rfcx.ranger.repo.TokenExpireException
import org.rfcx.ranger.repo.api.EventsApi
import org.rfcx.ranger.repo.api.MessageApi
import org.rfcx.ranger.repo.api.ReviewEventApi
import org.rfcx.ranger.service.LocationTrackerService
import org.rfcx.ranger.service.PullingAlertMessageReceiver
import org.rfcx.ranger.util.*


class MessageListActivity : AppCompatActivity(), OnMessageItemClickListener,
		OnCompleteListener<Void>,
		EventDialogFragment.OnAlertConfirmCallback,
		OnFailureListener, ReportEventDialogFragment.OnReportEventCallBack {
	
	
	lateinit var messageAdapter: MessageAdapter
	private lateinit var rangerRemote: FirebaseRemoteConfig
	
	companion object {
		fun startActivity(context: Context) {
			val intent = Intent(context, MessageListActivity::class.java)
			context.startActivity(intent)
		}
		
		private const val REQUEST_CODE_GOOGLE_AVAILABILITY = 100
		private const val REQUEST_PERMISSIONS_REQUEST_CODE = 34
	}
	
	override fun onStart() {
		super.onStart()
		checkGoogleApiAvailability()
	}
	
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_message_list)
		initToolbar()
		initAdapter()
		initRemoteConfig()
		fetchRangerRemoteConfig()
		startAlarmForMessageNotification()
		messageSwipeRefresh.setOnRefreshListener {
			fetchRangerRemoteConfig()
		}
		fab.setOnClickListener {
			if (this@MessageListActivity.isLocationAllow()) {
				ReportEventDialogFragment.newInstance().show(supportFragmentManager, ReportEventDialogFragment.tag)
			} else {
				requestPermissions()
			}
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
		
		if (!this.isLocationAllow()) {
			Log.w("Permission", "grant")
			requestPermissions()
		} else {
			startTrackerLocationService()
		}
		
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
			R.id.menu_ranger_group -> {
				val message = "You are connected to: " + (getSite() ?: "none")
				Snackbar.make(messageParentView, message, Snackbar.LENGTH_LONG).show()
			}
			R.id.menu_setting -> {
				startActivity(Intent(this@MessageListActivity, SettingActivity::class.java))
			}
		}
		return super.onOptionsItemSelected(item)
	}
	
	//region {@link addOnCompleteListener.onComplete} implementation
	override fun onComplete(task: Task<Void>) {
		if (task.isSuccessful) {
			Log.d(this@MessageListActivity.localClassName, "Fetch remote successful!")
			rangerRemote.activateFetched() // active config
			Log.d("Ranger remote", RemoteConfigKey.REMOTE_NOTI_FREQUENCY_DURATION + ": " + rangerRemote.getString(RemoteConfigKey.REMOTE_NOTI_FREQUENCY_DURATION))
			Log.d("Ranger remote", RemoteConfigKey.REMOTE_ENABLE_NOTI_MESSAGE + ": " + rangerRemote.getString(RemoteConfigKey.REMOTE_ENABLE_NOTI_MESSAGE))
			Log.d("Ranger remote", RemoteConfigKey.REMOTE_SHOW_EVENT_LIST + ": " + rangerRemote.getString(RemoteConfigKey.REMOTE_SHOW_EVENT_LIST))
			Log.d("Ranger remote", RemoteConfigKey.REMOTE_ENABLE_NOTI_EVENT_ALERT + ": " + rangerRemote.getString(RemoteConfigKey.REMOTE_ENABLE_NOTI_EVENT_ALERT))
		}
		getMessageList()
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
		messageAdapter = MessageAdapter(this@MessageListActivity)
		messageRecyclerView.setHasFixedSize(true)
		messageRecyclerView.layoutManager = LinearLayoutManager(this@MessageListActivity)
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
		Log.d(this@MessageListActivity.packageName, "Start fetch remote config!")
		// cache config
		var cacheExpiration: Long = 3600 // 1 hour
		if (rangerRemote.info.configSettings.isDeveloperModeEnabled) {
			cacheExpiration = 0
		}
		rangerRemote.fetch(cacheExpiration).addOnCompleteListener(this)
				.addOnFailureListener(this@MessageListActivity)
	}
	
	private fun getEvents(messageItems: MutableList<MessageItem>?) {
		if (!rangerRemote.getBoolean(RemoteConfigKey.REMOTE_SHOW_EVENT_LIST)) {
			messageSwipeRefresh.isRefreshing = false
			messageAdapter.updateMessages(messageItems)
			return
		}
		
		EventsApi().getEvents(this@MessageListActivity, 10, object : EventsApi.OnEventsCallBack {
			override fun onSuccess(event: EventResponse) {
				messageSwipeRefresh.isRefreshing = false
				event.events?.let {
					RealmHelper.getInstance().saveEvent(it)
				}
				
				val eventItems: ArrayList<EventItem>? = event.events?.mapTo(ArrayList()) {
					EventItem(it, BaseItem.ITEM_EVENT_TYPE, DateHelper.getDateTime(it.beginsAt))
				}
				
				val baseItems: ArrayList<BaseItem> = ArrayList()
				messageItems?.let { baseItems.addAll(it) }
				eventItems?.let { baseItems.addAll(it) }
				
				messageAdapter.updateMessages(baseItems)
			}
			
			override fun onFailed(t: Throwable?, message: String?) {
				messageSwipeRefresh.isRefreshing = false
				if (t is TokenExpireException) {
					logout()
					return
				}
				val error: String = if (message.isNullOrEmpty()) getString(R.string.error_common) else message!!
				Snackbar.make(messageParentView, error, Snackbar.LENGTH_LONG).show()
			}
		})
	}
	
	private fun getMessageList() {
		messageSwipeRefresh.isRefreshing = true
		
		MessageApi().getMessage(this@MessageListActivity, object : MessageApi.OnMessageCallBack {
			override fun onSuccess(messages: List<Message>) {
				
				RealmHelper.getInstance().saveMessage(messages)
				val messageItems: MutableList<MessageItem> = messages.mapTo(ArrayList()) {
					MessageItem(it, BaseItem.ITEM_MESSAGE_TYPE, DateHelper.getDateTime(it.time))
				}
				
				getEvents(messageItems)
			}
			
			override fun onFailed(t: Throwable?, message: String?) {
				messageSwipeRefresh.isRefreshing = false
				if (t is TokenExpireException) {
					logout()
					return
				}
				val error: String = if (message.isNullOrEmpty()) getString(R.string.error_common) else message!!
				Snackbar.make(rootView, error, Snackbar.LENGTH_LONG).show()
			}
		}
		)
	}
	
	private fun logout() {
		PreferenceHelper.getInstance(this@MessageListActivity).clear()
		LoginActivity.startActivity(this@MessageListActivity)
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
				
				Log.d("onMessageItemClick", "Event is opened" + RealmHelper.getInstance().isOpenedMessage(it))
				
				RealmHelper.getInstance().updateOpenedMessage(it)
			}
		} else if (item is EventItem) {
			RealmHelper.getInstance().updateOpenedEvent(item.event)
			showAlertPopup(item.event)
		}
		
		messageAdapter.notifyDataSetChanged()
	}
	
	override fun onCurrentAlert(event: Event) {
		RealmHelper.getInstance().updateConfirmedEvent(event)
		messageAdapter.notifyDataSetChanged()
		reportEvent(event, true)
	}
	
	override fun onIncorrectAlert(event: Event) {
		reportEvent(event, false)
	}
	
	
	override fun onReportSuccess() {
		Snackbar.make(messageParentView, R.string.report_send_success, Snackbar.LENGTH_LONG).show()
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
		val shouldProvideRationale = ActivityCompat.shouldShowRequestPermissionRationale(this@MessageListActivity,
				Manifest.permission.ACCESS_FINE_LOCATION)
		
		if (shouldProvideRationale) {
			val dialogBuilder: AlertDialog.Builder =
					AlertDialog.Builder(this@MessageListActivity).apply {
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
			
		} else {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
				requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
						REQUEST_PERMISSIONS_REQUEST_CODE)
			}
		}
	}
	
	override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>,
	                                        grantResults: IntArray) {
		Log.d("onRequestPermission", "onRequestPermissionsResult: " + requestCode + permissions.toString())
		if (requestCode == REQUEST_PERMISSIONS_REQUEST_CODE) {
			if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
				startTrackerLocationService()
			}
		}
	}
	
	private fun showAlertPopup(event: Event) {
		Log.d("onMessageItemClick", "Event is opened" + RealmHelper.getInstance().isOpenedEvent(event))
		RealmHelper.getInstance().updateOpenedEvent(event)
		EventDialogFragment.newInstance(event).show(supportFragmentManager, null)
	}
	
	private fun reportEvent(event: Event, isConfirmEvent: Boolean) {
		ReviewEventApi().reViewEvent(this@MessageListActivity, event,
				isConfirmEvent, object : ReviewEventApi.ReviewEventCallback {
			override fun onSuccess() {
				Log.d("reportEvent", "onSuccess $isConfirmEvent")
				if (!isConfirmEvent) {
					//refresh list if user report reject
					getMessageList()
				}
			}
			
			override fun onFailed(t: Throwable?, message: String?) {
				Log.d("reportEvent", "onFailed $message")
			}
			
		})
	}
	
	// start Alarm manger for repeat to call PullingAlertMessageReceiver every 60 sec.
	private fun startAlarmForMessageNotification() {
		PullingAlertMessageReceiver.startAlarmForMessageNotification(this@MessageListActivity,
				rangerRemote.getLong(RemoteConfigKey.REMOTE_NOTI_FREQUENCY_DURATION))
	}
	
	private fun startTrackerLocationService() {
		if (PreferenceHelper.getInstance(this).getString(PrefKey.ENABLE_LOCATION_TRACKING, "")
				!= SettingActivity.TRACKING_OFF) {
			val intent = Intent(this@MessageListActivity, LocationTrackerService::class.java)
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
				startForegroundService(intent)
			}else{
				startService(intent)
			}
		}
	}
	
	
}
