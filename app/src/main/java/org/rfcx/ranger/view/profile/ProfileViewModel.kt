package org.rfcx.ranger.view.profile

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import org.rfcx.ranger.BuildConfig
import org.rfcx.ranger.data.local.ProfileData

class ProfileViewModel(private val profileData: ProfileData) : ViewModel() {
	
	val locationTracking = MutableLiveData<Boolean>()
	val notificationReceiving = MutableLiveData<Boolean>()
	val userSite = MutableLiveData<String>()
	val appVersion = MutableLiveData<String>()
	val userName = MutableLiveData<String>()
	
	init {
		locationTracking.value = profileData.getTracking()
		notificationReceiving.value = profileData.getReceiveNotification()
		userSite.value = profileData.getSiteName()
		appVersion.value = BuildConfig.VERSION_NAME
		userName.value = profileData.getUserNickname()
	}
	
	
	fun onReceiving(enable: Boolean) {
		profileData.updateReceivingNotification(enable)
		notificationReceiving.value = enable
	}
	
	fun onTracingStatusChange() {
		locationTracking.value = profileData.getTracking()
	}
}