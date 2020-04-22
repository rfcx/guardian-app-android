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
import org.rfcx.ranger.data.local.ProfileData
import org.rfcx.ranger.data.remote.subscribe.SubscribeUseCase
import org.rfcx.ranger.data.remote.subscribe.unsubscribe.UnsubscribeUseCase
import org.rfcx.ranger.entity.SubscribeRequest
import org.rfcx.ranger.entity.SubscribeResponse
import org.rfcx.ranger.util.*

class ProfileViewModel(private val context: Context, private val profileData: ProfileData, private val subscribeUseCase: SubscribeUseCase, private val unsubscribeUseCase: UnsubscribeUseCase) : ViewModel() {
	
	val locationTracking = MutableLiveData<Boolean>()
	val notificationReceiving = MutableLiveData<Boolean>()
	val notificationReceivingByEmail = MutableLiveData<Boolean>()
	val userSite = MutableLiveData<String>()
	val appVersion = MutableLiveData<String>()
	val userName = MutableLiveData<String>()
	val sendToEmail = MutableLiveData<String>()
	val guardianGroup = MutableLiveData<String>()
	val formatCoordinates = MutableLiveData<String>()
	private val offlineManager: OfflineManager = OfflineManager.getInstance(context)
	lateinit var definition: OfflineTilePyramidRegionDefinition
	private val viewModelJob = Job()
	private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)
	
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
	}
	
	fun resumed() {
		getSiteName()
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
	
	fun offlineMapBox() {
		// todo onCleared() ->  viewModelJob.cancel()
		offlineManager.setOfflineMapboxTileCountLimit(10000) // what?
		val style = Style.OUTDOORS
		val latLngBounds: LatLngBounds = LatLngBounds.from(16.2000000, 100.56000, 16.1999909, 100.551860)
		definition = OfflineTilePyramidRegionDefinition(
				style, latLngBounds, 10.0, 20.0,
				context.resources.displayMetrics.density
		)
		offlineManager.createOfflineRegion(definition, METADATA.toByteArray(),
				object : OfflineManager.CreateOfflineRegionCallback {
					override fun onCreate(offlineRegion: OfflineRegion) {
						uiScope.launch { createOfflineRegion(offlineRegion) }
					}
					
					override fun onError(error: String) {
						Log.e(TAG, "Error: $error")
					}
				})
	}
	
	private suspend fun createOfflineRegion(offlineRegion: OfflineRegion) {
		Log.d(TAG, "createOfflineRegion")
		withContext(Dispatchers.IO) {
			offlineRegion.setDownloadState(OfflineRegion.STATE_ACTIVE)
			offlineRegion.setObserver(object : OfflineRegion.OfflineRegionObserver {
				
				private var percentage: Int = -1
				
				override fun onStatusChanged(status: OfflineRegionStatus) {
					val required = status.requiredResourceCount
					val oldPercentage = this.percentage
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
					// Todo show % download
						Log.d(
								TAG, if (percentage >= 100)
							"Region downloaded successfully."
						else "$percentage% of region downloaded"
						)
				}
				
				override fun onError(error: OfflineRegionError) {
					Log.e(TAG, "onError reason: ${error.reason}")
					Log.e(TAG, "onError message: ${error.message}")
				}
				
				override fun mapboxTileCountLimitExceeded(limit: Long) {
					Log.e(TAG, "Mapbox tile count limit exceeded: $limit")
				}
				
			})
		}
	}
	
	companion object {
		private const val METADATA = "{\"regionName\":\"Tembe\"}"
		private const val TAG = "1234"
	}
}