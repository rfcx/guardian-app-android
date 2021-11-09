package org.rfcx.incidents.view

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import org.rfcx.incidents.data.local.ProfileData
import org.rfcx.incidents.entity.Stream
import org.rfcx.incidents.entity.response.Response
import org.rfcx.incidents.localdb.ResponseDb
import org.rfcx.incidents.localdb.StreamDb
import org.rfcx.incidents.service.ReviewEventSyncWorker
import org.rfcx.incidents.util.CredentialKeeper
import org.rfcx.incidents.util.asLiveData

class MainActivityViewModel(private val profileData: ProfileData,
                            private val responseDb: ResponseDb,
                            private val streamDb: StreamDb,
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
	
	fun getStreamByName(name: String): Stream? = streamDb.getStreamByName(name)
	
}
