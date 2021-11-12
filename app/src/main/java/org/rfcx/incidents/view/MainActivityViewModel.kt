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

class MainActivityViewModel(private val responseDb: ResponseDb,
                            private val streamDb: StreamDb,
                            credentialKeeper: CredentialKeeper) : ViewModel() {
	
	val isRequireToLogin = MutableLiveData<Boolean>()
	
	fun getResponses(): LiveData<List<Response>> {
		return Transformations.map(responseDb.getAllResultsAsync().asLiveData()) { it }
	}
	
	val eventGuIdFromNotification = MutableLiveData<String>()
	
	init {
		isRequireToLogin.value = !credentialKeeper.hasValidCredentials()
		ReviewEventSyncWorker.enqueue()
	}
	
	fun getStreamByName(name: String): Stream? = streamDb.getStreamByName(name)
	
}
