package org.rfcx.ranger.view

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import org.rfcx.ranger.data.local.ProfileData
import org.rfcx.ranger.entity.response.Response
import org.rfcx.ranger.localdb.ResponseDb
import org.rfcx.ranger.service.ReviewEventSyncWorker
import org.rfcx.ranger.util.CredentialKeeper
import org.rfcx.ranger.util.asLiveData

class MainActivityViewModel(private val profileData: ProfileData,
                            private val responseDb: ResponseDb,
                            credentialKeeper: CredentialKeeper) : ViewModel() {
	
	val isRequireToLogin = MutableLiveData<Boolean>()
	
	private val _isLocationTrackingOn = MutableLiveData<Boolean>()
	val isLocationTrackingOn = _isLocationTrackingOn
	
	fun getResponses(): LiveData<List<Response>> {
		return Transformations.map(responseDb.getAllResultsAsync().asLiveData()) { it }
	}
	
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
