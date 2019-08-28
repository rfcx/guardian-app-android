package org.rfcx.ranger.view

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import org.rfcx.ranger.data.local.ProfileData
import org.rfcx.ranger.util.CredentialKeeper

class MainActivityViewModel(profileData: ProfileData, credentialKeeper: CredentialKeeper) : ViewModel() {
	
	val isRequireToLogin = MutableLiveData<Boolean>()
	
	val isLocationTrackingOn = MutableLiveData<Boolean>()
	
	init {
		isRequireToLogin.value = !credentialKeeper.hasValidCredentials()
		isLocationTrackingOn.value = profileData.getTracking()
	}
}