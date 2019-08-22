package org.rfcx.ranger.view.profile

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import org.rfcx.ranger.BuildConfig
import org.rfcx.ranger.data.local.ProfileData
import org.rfcx.ranger.util.getGuardianGroup

class ProfileViewModel(private val context: Context, private val profileData: ProfileData) : ViewModel() {
	
	val locationTracking = MutableLiveData<Boolean>()
	val notificationReceiving = MutableLiveData<Boolean>()
	val userSite = MutableLiveData<String>()
	val appVersion = MutableLiveData<String>()
	val userName = MutableLiveData<String>()
	val guardianGroup = MutableLiveData<String>()
	
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
	
	fun updateSiteName() {
		guardianGroup.value = context.getGuardianGroup()
	}
}