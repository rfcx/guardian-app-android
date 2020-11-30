package org.rfcx.ranger.view.profile

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mapbox.mapboxsdk.geometry.LatLngBounds
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.offline.*
import io.reactivex.observers.DisposableSingleObserver
import kotlinx.coroutines.*
import org.rfcx.ranger.BuildConfig
import org.rfcx.ranger.R
import org.rfcx.ranger.data.local.EventDb
import org.rfcx.ranger.data.local.ProfileData
import org.rfcx.ranger.data.remote.site.GetSiteNameUseCase
import org.rfcx.ranger.data.remote.subscribe.SubscribeUseCase
import org.rfcx.ranger.data.remote.subscribe.unsubscribe.UnsubscribeUseCase
import org.rfcx.ranger.entity.SubscribeRequest
import org.rfcx.ranger.entity.SubscribeResponse
import org.rfcx.ranger.entity.event.Event
import org.rfcx.ranger.entity.site.SiteResponse
import org.rfcx.ranger.util.*
import kotlin.random.Random

class ProfileViewModel(private val context: Context, private val profileData: ProfileData, private val getSiteName: GetSiteNameUseCase, private val subscribeUseCase: SubscribeUseCase, private val unsubscribeUseCase: UnsubscribeUseCase, private val eventDb: EventDb) : ViewModel() {
	
	val locationTracking = MutableLiveData<Boolean>()
	val notificationReceiving = MutableLiveData<Boolean>()
	val notificationReceivingByEmail = MutableLiveData<Boolean>()
	val userSite = MutableLiveData<String>()
	val appVersion = MutableLiveData<String>()
	val userName = MutableLiveData<String>()
	val downloaded = MutableLiveData<String>()
	val deleteText = MutableLiveData<String>()
	val showDownload = MutableLiveData<Boolean>()
	val showUnavailable = MutableLiveData<Boolean>()
	val showLoading = MutableLiveData<Boolean>()
	val showDelete = MutableLiveData<Boolean>()
	val showPercent = MutableLiveData<Boolean>()
	val sendToEmail = MutableLiveData<String>()
	val guardianGroup = MutableLiveData<String>()
	val formatCoordinates = MutableLiveData<String>()
	val showNotificationByEmail = MutableLiveData<Boolean>()
	
	private val offlineManager: OfflineManager = OfflineManager.getInstance(context)
	lateinit var definition: OfflineTilePyramidRegionDefinition
	private val viewModelJob = Job()
	private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)
	val preferences = Preferences.getInstance(context)
	
	private val _logoutState = MutableLiveData<Boolean>()
	val logoutState: LiveData<Boolean> = _logoutState
	
	init {
		getSiteName()
		locationTracking.value = profileData.getTracking()
		notificationReceiving.value = profileData.getReceiveNotification()
		notificationReceivingByEmail.value = profileData.getReceiveNotificationByEmail()
		appVersion.value = "${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE}) "
		userName.value = profileData.getUserNickname()
		sendToEmail.value = "${context.getString(R.string.sent_to)} ${context.getUserEmail()}"
		formatCoordinates.value = "${context.getCoordinatesFormat()}"
		deleteText.value = preferences.getString(Preferences.DELETE_TEXT, "DELETE")
		showNotificationByEmail.value = context.getUserEmail() != ""
	}
	
	private fun setViewMapOffline(state: String) {
		when (state) {
			UNAVAILABLE -> {
				showLoading.value = false
				showPercent.value = false
				showDownload.value = false
				showDelete.value = false
				showUnavailable.value = true
			}
			DOWNLOAD_STATE -> {
				showLoading.value = false
				showPercent.value = false
				showDownload.value = true
				showDelete.value = false
				showUnavailable.value = false
			}
			DOWNLOADING_STATE -> {
				showLoading.value = true
				showPercent.value = true
				showDownload.value = false
				showDelete.value = false
				showUnavailable.value = false
			}
			DOWNLOADED_STATE -> {
				showLoading.value = false
				showPercent.value = false
				showDownload.value = false
				showDelete.value = true
				showUnavailable.value = false
			}
			DELETING_STATE -> {
				showLoading.value = true
				showPercent.value = false
				showDownload.value = false
				showDelete.value = false
				showUnavailable.value = false
			}
		}
	}
	
	fun resumed() {
		getSiteName()
		getSiteBounds()
		formatCoordinates.value = "${context.getCoordinatesFormat()}"
	}
	
	private fun getSiteName() {
		val site = Preferences.getInstance(context).getString(Preferences.SITE_FULLNAME)
		if (site.isNullOrEmpty()) {
			userSite.value = profileData.getSiteName()
		} else {
			userSite.value = site
		}
	}
	
	private fun getSiteBounds() {
		val siteBounds = preferences.getBoolean(Preferences.HAVE_SITE_BOUNDS)
		
		getSiteName.execute(object : DisposableSingleObserver<List<SiteResponse>>() {
			override fun onSuccess(t: List<SiteResponse>) {
				preferences.putString(Preferences.SITE_TIMEZONE, t[0].timezone)
				setUnavailable(t[0].bounds != null)
			}
			
			override fun onError(e: Throwable) {
				Log.d("getSiteName", "error $e")
			}
		}, profileData.getDefaultSiteId())
		
		setUnavailable(siteBounds)
	}
	
	fun setUnavailable(haveSiteBounds: Boolean) {
		if (haveSiteBounds) {
			val state = preferences.getString(Preferences.OFFLINE_MAP_STATE, DOWNLOAD_STATE)
			if (state == DOWNLOAD_CANCEL_STATE) {
				offlineMapBox()
			} else {
				setViewMapOffline(state)
			}
		} else {
			setViewMapOffline(UNAVAILABLE)
		}
	}
	
	fun setStateOfflineMap(state: String) {
		preferences.putString(Preferences.OFFLINE_MAP_STATE, state)
		setViewMapOffline(state)
	}
	
	fun onReceiving(enable: Boolean) {
		profileData.updateReceivingNotification(enable)
		notificationReceiving.value = enable
		// set messaging
		if (enable) {
			CloudMessaging.subscribeIfRequired(context)
		} else {
			CloudMessaging.unsubscribe(context)
		}
	}
	
	fun onReceivingByEmail(enable: Boolean) {
		if (profileData.hasGuardianGroup()) {
			// call api
			if (enable) {
				if (!profileData.getReceiveNotificationByEmail()) {
					onSubscribe()
					
					profileData.updateReceivingNotificationByEmail(enable)
					notificationReceivingByEmail.value = enable
				}
			} else {
				onUnsubscribe()
				profileData.updateReceivingNotificationByEmail(enable)
				notificationReceivingByEmail.value = enable
			}
		} else {
			notificationReceivingByEmail.value = false
			Toast.makeText(context, context.getString(R.string.please_set_guardian_group), Toast.LENGTH_SHORT).show()
		}
	}
	
	fun onLogout() {
		deleteOfflineRegion(true)
		_logoutState.value = true
		if (profileData.getReceiveNotificationByEmail()) {
			unsubscribeUseCase.execute(object : DisposableSingleObserver<SubscribeResponse>() {
				override fun onSuccess(t: SubscribeResponse) {
					_logoutState.value = false
					context.logout()
				}
				
				override fun onError(e: Throwable) {
					_logoutState.value = false
				}
			}, SubscribeRequest(listOf(context.getGuardianGroup().toString())))
		} else {
			context.logout()
		}
	}
	
	private fun onSubscribe() {
		subscribeUseCase.execute(object : DisposableSingleObserver<SubscribeResponse>() {
			override fun onSuccess(t: SubscribeResponse) {
			}

			override fun onError(e: Throwable) {
				profileData.updateReceivingNotificationByEmail(false)
				notificationReceivingByEmail.value = false
				Toast.makeText(context, context.getString(R.string.error_common), Toast.LENGTH_SHORT).show()
			}
		}, SubscribeRequest(listOf(context.getGuardianGroup().toString())))
	}
	
	private fun onUnsubscribe() {
		unsubscribeUseCase.execute(object : DisposableSingleObserver<SubscribeResponse>() {
			override fun onSuccess(t: SubscribeResponse) {
			}
			
			override fun onError(e: Throwable) {
				profileData.updateReceivingNotificationByEmail(true)
				notificationReceivingByEmail.value = true
				Toast.makeText(context, context.getString(R.string.error_common), Toast.LENGTH_SHORT).show()
			}
		}, SubscribeRequest(listOf(context.getGuardianGroup().toString())))
	}
	
	fun onTracingStatusChange() {
		locationTracking.value = profileData.getTracking()
	}
	
	fun updateSiteName() {
		guardianGroup.value = Preferences.getInstance(context).getString(Preferences.SELECTED_GUARDIAN_GROUP_FULLNAME)
	}
	
	fun randomGuidOfAlert(): Event? {
		val events = eventDb.getEvents()
		if (events.isNullOrEmpty()) return null
		val index = Random.nextInt(events.size)
		
		return events[index]
	}
	
	fun offlineMapBox() {
		if (context.isNetworkAvailable()) {
			val preferences = Preferences.getInstance(context)
			val minLat = preferences.getString(Preferences.MIN_LATITUDE)
			val maxLat = preferences.getString(Preferences.MAX_LATITUDE)
			val minLng = preferences.getString(Preferences.MIN_LONGITUDE)
			val maxLng = preferences.getString(Preferences.MAX_LONGITUDE)
			
			setStateOfflineMap(DOWNLOADING_STATE)
			
			offlineManager.setOfflineMapboxTileCountLimit(10000)
			val style = Style.OUTDOORS
			if (minLat !== null && maxLat !== null && minLng !== null && maxLng !== null) {
				val latLngBounds: LatLngBounds = LatLngBounds.from(maxLat.toDouble(), maxLng.toDouble(), minLat.toDouble(), minLng.toDouble())
				definition = OfflineTilePyramidRegionDefinition(style, latLngBounds, 10.0, 15.0, context.resources.displayMetrics.density)
				offlineManager.createOfflineRegion(definition, METADATA.toByteArray(),
						object : OfflineManager.CreateOfflineRegionCallback {
							override fun onCreate(offlineRegion: OfflineRegion) {
								uiScope.launch { createOfflineRegion(offlineRegion) }
							}
							
							override fun onError(error: String) {
								setViewMapOffline(DOWNLOAD_STATE)
								Log.e(TAG, "Error: $error")
							}
						})
			}
		} else {
			Toast.makeText(context, context.getString(R.string.no_internet_connection), Toast.LENGTH_SHORT).show()
		}
	}
	
	fun deleteOfflineRegion(isLogout: Boolean) {
		if (context.isNetworkAvailable()) {
			if (!isLogout) {
				setStateOfflineMap(DELETING_STATE)
			}
			
			val offlineManager = OfflineManager.getInstance(context)
			offlineManager?.listOfflineRegions(object : OfflineManager.ListOfflineRegionsCallback {
				override fun onList(offlineRegions: Array<out OfflineRegion>?) {
					if (offlineRegions?.size != null) {
						if (offlineRegions.isNotEmpty()) {
							onDeleteOfflineRegion(offlineRegions[0], isLogout)
						}
					}
				}
				
				override fun onError(error: String?) {
					if (!isLogout) {
						setStateOfflineMap(DOWNLOADED_STATE)
					}
				}
			})
		} else {
			Toast.makeText(context, context.getString(R.string.no_internet_connection), Toast.LENGTH_SHORT).show()
		}
	}
	
	fun onDeleteOfflineRegion(offRegion: OfflineRegion, isLogout: Boolean) {
		offRegion.delete(object : OfflineRegion.OfflineRegionDeleteCallback {
			override fun onDelete() {
				if (!isLogout) {
					setStateOfflineMap(DOWNLOAD_STATE)
				}
			}
			
			override fun onError(error: String) {
				if (!isLogout) {
					setStateOfflineMap(DOWNLOADED_STATE)
				}
			}
		})
	}
	
	private suspend fun createOfflineRegion(offlineRegion: OfflineRegion) {
		withContext(Dispatchers.IO) {
			offlineRegion.setDownloadState(OfflineRegion.STATE_ACTIVE)
			offlineRegion.setObserver(object : OfflineRegion.OfflineRegionObserver {
				
				private var percentage: Int = -1
				
				override fun onStatusChanged(status: OfflineRegionStatus) {
					val required = status.requiredResourceCount
					val oldPercentage = this.percentage
					val megabybtes = status.completedResourceSize / 1048576
					val percentage: Int = when {
						status.isComplete -> {
							101
						}
						required > 0L ->
							(100 * status.completedResourceCount / required).toInt()
						else -> 0
					}
					this.percentage = percentage
					if (percentage > oldPercentage)
						if (percentage >= 100) {
							deleteText.value = context.getString(R.string.delete, megabybtes.toString())
							preferences.putString(Preferences.DELETE_TEXT, context.getString(R.string.delete, megabybtes.toString()))
							setStateOfflineMap(DOWNLOADED_STATE)
						} else {
							setStateOfflineMap(DOWNLOADING_STATE)
							downloaded.value = "$percentage %"
						}
				}
				
				override fun onError(error: OfflineRegionError) {
					setStateOfflineMap(DOWNLOAD_STATE)
				}
				
				override fun mapboxTileCountLimitExceeded(limit: Long) {
					Log.e(TAG, "Mapbox tile count limit exceeded: $limit")
				}
			})
		}
	}
	
	companion object {
		private const val METADATA = "{\"regionName\":\"Tembe\"}"
		private const val TAG = "ProfileViewModel"
		private const val DELETING_STATE = "DELETING_STATE"
		private const val DOWNLOAD_STATE = "DOWNLOAD_STATE"
		private const val DOWNLOADED_STATE = "DOWNLOADED_STATE"
		private const val UNAVAILABLE = "UNAVAILABLE"
		const val DOWNLOAD_CANCEL_STATE = "DOWNLOAD_CANCEL_STATE"
		const val DOWNLOADING_STATE = "DOWNLOADING_STATE"
	}
}
