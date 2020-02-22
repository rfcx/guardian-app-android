package org.rfcx.ranger.view.profile

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.reactivex.observers.DisposableSingleObserver
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
	
	init {
		getSiteName()
		locationTracking.value = profileData.getTracking()
		notificationReceiving.value = profileData.getReceiveNotification()
		notificationReceivingByEmail.value = profileData.getReceiveNotificationByEmail()
		appVersion.value = "${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE}) "
		userName.value = profileData.getUserNickname()
		sendToEmail.value = "sent to ${context.getUserEmail()}"
		
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
				if(!profileData.getReceiveNotificationByEmail()){
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
		if(profileData.getReceiveNotificationByEmail()){
			unsubscribeUseCase.execute(object : DisposableSingleObserver<SubscribeResponse>() {
				override fun onSuccess(t: SubscribeResponse) {
					context.logout()
				}
				
				override fun onError(e: Throwable) {
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
}