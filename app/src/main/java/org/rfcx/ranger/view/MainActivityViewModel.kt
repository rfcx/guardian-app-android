package org.rfcx.ranger.view

import androidx.lifecycle.*
import io.realm.RealmResults
import org.rfcx.ranger.data.local.EventDb
import org.rfcx.ranger.data.local.ProfileData
import org.rfcx.ranger.entity.event.Event
import org.rfcx.ranger.service.ReviewEventSyncWorker
import org.rfcx.ranger.util.CredentialKeeper
import org.rfcx.ranger.util.asLiveData

class MainActivityViewModel(private val profileData: ProfileData,
                            private val eventDb: EventDb,
                            credentialKeeper: CredentialKeeper) : ViewModel() {
	
	val isRequireToLogin = MutableLiveData<Boolean>()
	
	private val _isLocationTrackingOn = MutableLiveData<Boolean>()
	val isLocationTrackingOn = _isLocationTrackingOn
	
	val eventGuIdFromNotification = MutableLiveData<String>()
	
	private lateinit var eventLiveData: LiveData<List<Event>>
	private var _alertCount = MutableLiveData<Int>()
	val alertCount: LiveData<Int>
		get() = _alertCount
	
	private val eventCountObserve = Observer<List<Event>> {
		val cacheEvents = eventDb.getEvents()
		_alertCount.value = eventDb.lockReviewEventUnread(cacheEvents).size
	}
	
	init {
		fetchEvents()
		isRequireToLogin.value = !credentialKeeper.hasValidCredentials()
		_isLocationTrackingOn.value = profileData.getTracking()
		
		ReviewEventSyncWorker.enqueue()
	}
	
	private fun fetchEvents() {
		eventLiveData = Transformations.map<RealmResults<Event>,
				List<Event>>(eventDb.getAllResultsAsync().asLiveData()) {
			it
		}
		eventLiveData.observeForever(eventCountObserve)
	}
	
	fun updateLocationTracking() {
		_isLocationTrackingOn.value = profileData.getTracking()
	}
}
