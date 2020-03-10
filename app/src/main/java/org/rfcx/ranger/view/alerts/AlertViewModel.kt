package org.rfcx.ranger.view.alerts

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import org.rfcx.ranger.data.local.ProfileData
import org.rfcx.ranger.data.remote.ResponseCallback
import org.rfcx.ranger.data.remote.domain.alert.GetEventsUseCase
import org.rfcx.ranger.entity.event.Event
import org.rfcx.ranger.entity.event.EventsRequestFactory

class AlertViewModel(private val context: Context, private val profileData: ProfileData,
                     private val eventsUserCase: GetEventsUseCase) : ViewModel() {
	
	var hasGuardianGroup = profileData.hasGuardianGroup()
	
	val eventIdFromNotification = MutableLiveData<String>()
	
	private val _observeGuardianGroup = MutableLiveData<Boolean>()
	val observeGuardianGroup: LiveData<Boolean> = _observeGuardianGroup
	private var _isLoading: Boolean = false
	
	fun resumed() {
		hasGuardianGroup = profileData.hasGuardianGroup()
		_observeGuardianGroup.value = profileData.hasGuardianGroup()
		loadAlerts()
	}
	
	private fun loadAlerts() {
		_isLoading = true
		val group = profileData.getGuardianGroup() ?: return
		
		val requestFactory = EventsRequestFactory(listOf(group.shortname), "measured_at", "DESC",
				AllAlertsViewModel.PAGE_LIMITS, 0, group.values)
		
		eventsUserCase.execute(object : ResponseCallback<Pair<List<Event>, Int>> {
			override fun onSuccess(t: Pair<List<Event>, Int>) {
				_isLoading = false
			}
			
			override fun onError(e: Throwable) {
				_isLoading = false
			}
		}, requestFactory)
	}
}