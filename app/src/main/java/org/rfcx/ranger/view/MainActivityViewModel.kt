package org.rfcx.ranger.view

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.reactivex.observers.DisposableSingleObserver
import org.rfcx.ranger.data.local.ProfileData
import org.rfcx.ranger.data.remote.domain.alert.GetEventsUseCase
import org.rfcx.ranger.entity.event.Event
import org.rfcx.ranger.entity.event.EventResponse
import org.rfcx.ranger.entity.event.EventsRequestFactory
import org.rfcx.ranger.service.DownLoadEventWorker
import org.rfcx.ranger.util.CredentialKeeper
import org.rfcx.ranger.view.alerts.AllAlertsViewModel

class MainActivityViewModel(private val profileData: ProfileData, credentialKeeper: CredentialKeeper,
                            private val getEventsUseCase: GetEventsUseCase) : ViewModel() {
	
	val isRequireToLogin = MutableLiveData<Boolean>()
	
	val isLocationTrackingOn = MutableLiveData<Boolean>()
	
	val eventFromNotification = MutableLiveData<Event>()
	
	init {
		isRequireToLogin.value = !credentialKeeper.hasValidCredentials()
		isLocationTrackingOn.value = profileData.getTracking()
	}
	
	fun getEventAndPreloadAudio() {
		val guardianGroup = profileData.getGuardianGroup() ?: return
		val requestFactory = EventsRequestFactory(guardianGroup, "begins_at", "DESC",
				AllAlertsViewModel.PAGE_LIMITS, 0)
		getEventsUseCase.execute(object : DisposableSingleObserver<EventResponse>() {
			override fun onSuccess(t: EventResponse) {
				DownLoadEventWorker.enqueue()
			}
			
			override fun onError(e: Throwable) {
			
			}
		}, requestFactory)
	}
}
