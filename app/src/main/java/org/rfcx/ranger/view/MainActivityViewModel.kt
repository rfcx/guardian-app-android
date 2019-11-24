package org.rfcx.ranger.view

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import org.rfcx.ranger.data.local.ProfileData
import org.rfcx.ranger.entity.event.Event
import org.rfcx.ranger.service.ReviewEventSyncWorker
import org.rfcx.ranger.util.CredentialKeeper

class MainActivityViewModel(private val profileData: ProfileData, credentialKeeper: CredentialKeeper) : ViewModel() {
	
	val isRequireToLogin = MutableLiveData<Boolean>()
	
	private val _isLocationTrackingOn = MutableLiveData<Boolean>()
	val isLocationTrackingOn = _isLocationTrackingOn
	
	val eventGuIdFromNotification = MutableLiveData<String>()
	
	init {
		isRequireToLogin.value = !credentialKeeper.hasValidCredentials()
		_isLocationTrackingOn.value = profileData.getTracking()
		
		ReviewEventSyncWorker.enqueue()
	}
	
	fun updateLocationTracking() {
		_isLocationTrackingOn.value = profileData.getTracking()
	}
}
