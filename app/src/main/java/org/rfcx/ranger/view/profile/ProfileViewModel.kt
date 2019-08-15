package org.rfcx.ranger.view.profile

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import org.rfcx.ranger.BuildConfig
import org.rfcx.ranger.util.LocationTracking
import org.rfcx.ranger.util.Preferences
import org.rfcx.ranger.util.getSiteName
import org.rfcx.ranger.util.getUserNickname

class ProfileViewModel(private val prefManager: Preferences) : ViewModel(){

    val locationTracking = MutableLiveData<Boolean>()
    val notificationReceiving = MutableLiveData<Boolean>()
    val userSite = MutableLiveData<String>()
    val appVersion = MutableLiveData<String>()
    val userName = MutableLiveData<String>()

    init{
        locationTracking.value = when(prefManager.getString(Preferences.ENABLE_LOCATION_TRACKING)){
            LocationTracking.TRACKING_ON -> true
            else -> false
        }
        notificationReceiving.value = prefManager.getBoolean(Preferences.SHOULD_RECEIVE_EVENT_NOTIFICATIONS)
        userSite.value = prefManager.getSiteName()
        appVersion.value = BuildConfig.VERSION_NAME
        userName.value = prefManager.getUserNickname()
    }

    fun onTracking(enable: Boolean){
        prefManager.putString(Preferences.ENABLE_LOCATION_TRACKING, if (enable) LocationTracking.TRACKING_ON else LocationTracking.TRACKING_OFF)
        locationTracking.value = enable
    }

    fun onReceiving(enable: Boolean){
        prefManager.putBoolean(Preferences.SHOULD_RECEIVE_EVENT_NOTIFICATIONS, enable)
        notificationReceiving.value = enable
    }
}