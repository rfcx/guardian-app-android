package org.rfcx.ranger.view.profile

import android.content.Context
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.reactivex.observers.DisposableSingleObserver
import org.rfcx.ranger.BuildConfig
import org.rfcx.ranger.data.local.ProfileData
import org.rfcx.ranger.data.remote.subscribe.SubscribeUseCase
import org.rfcx.ranger.entity.SubscribeRequest
import org.rfcx.ranger.entity.SubscribeResponse
import org.rfcx.ranger.util.CloudMessaging
import org.rfcx.ranger.util.Preferences
import org.rfcx.ranger.util.getGuardianGroup
import org.rfcx.ranger.util.logout

class ProfileViewModel(private val context: Context, private val profileData: ProfileData, private val subscribeUseCase: SubscribeUseCase) : ViewModel() {
	
	val locationTracking = MutableLiveData<Boolean>()
	val notificationReceiving = MutableLiveData<Boolean>()
	val userSite = MutableLiveData<String>()
	val appVersion = MutableLiveData<String>()
	val userName = MutableLiveData<String>()
	val guardianGroup = MutableLiveData<String>()
	
	init {
		getSiteName()
		locationTracking.value = profileData.getTracking()
		notificationReceiving.value = profileData.getReceiveNotification()
		appVersion.value = "${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE}) "
		userName.value = profileData.getUserNickname()
	}
	
	private fun getSiteName() {
		val site = Preferences.getInstance(context).getString(Preferences.SITE_FULLNAME)
		
		if(site.isNullOrEmpty()){
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
	
	fun onLogout() {
		context.logout()
	}
	
	fun onSubscribe() {
		subscribeUseCase.execute(object : DisposableSingleObserver<SubscribeResponse>() {
			override fun onSuccess(t: SubscribeResponse) {
				Log.d("onSubscribe","${t.success}")
			}

			override fun onError(e: Throwable) {
				Log.d("onSubscribe","$e")
			}
		}, SubscribeRequest(listOf(context.getGuardianGroup().toString())))
	}
	
	fun onTracingStatusChange() {
		locationTracking.value = profileData.getTracking()
	}
	
	fun updateSiteName() {
		guardianGroup.value = Preferences.getInstance(context).getString(Preferences.SELECTED_GUARDIAN_GROUP_FULLNAME)
	}
}