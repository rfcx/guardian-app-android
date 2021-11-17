package org.rfcx.incidents.view

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import org.rfcx.incidents.data.local.ProfileData

class LocationTrackingViewModel(private val profileData: ProfileData) : ViewModel() {
	
	private val _locationTrackingState = MutableLiveData<Boolean>()
	val locationTrackingState: LiveData<Boolean> = _locationTrackingState
	
	private val _requireLocationTrackingState = MutableLiveData<Boolean>()
	val requireLocationTrackingState: LiveData<Boolean> = _requireLocationTrackingState
	
	
	fun requireDisableLocationTracking() {
		_requireLocationTrackingState.value = false
	}
	
	fun requireEnableLocationTracking() {
		_requireLocationTrackingState.value = true
	}
	
	fun trackingStateChange() {
		_locationTrackingState.value = profileData.getTracking()
	}
	
}
