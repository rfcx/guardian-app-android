package org.rfcx.ranger.view.alerts

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.reactivex.observers.DisposableSingleObserver
import org.rfcx.ranger.R
import org.rfcx.ranger.data.local.ProfileData
import org.rfcx.ranger.data.remote.domain.alert.GetEventsUseCase
import org.rfcx.ranger.entity.event.Event
import org.rfcx.ranger.entity.event.EventResponse
import org.rfcx.ranger.entity.event.EventsRequestFactory
import org.rfcx.ranger.entity.event.EventsResponse
import org.rfcx.ranger.service.DownLoadEventWorker
import org.rfcx.ranger.util.getGuardianGroup
import org.rfcx.ranger.util.getResultError

class AlertViewModel(private val context: Context, private val profileData: ProfileData, private val eventsUserCase: GetEventsUseCase) : ViewModel() {
	
	var hasGuardianGroup = profileData.hasGuardianGroup()
	
	val eventFromNotification = MutableLiveData<Event>()
	
	private val _observeGuardianGroup = MutableLiveData<Boolean>()
	val observeGuardianGroup: LiveData<Boolean> = _observeGuardianGroup
	
	fun resumed() {
		hasGuardianGroup = profileData.hasGuardianGroup()
		_observeGuardianGroup.value = profileData.hasGuardianGroup()
	}
	
	fun loadAlerts(){
		val group = context.getGuardianGroup()
		if (group == null) {
			Toast.makeText(context, context.getString(R.string.error_no_guardian_group_set), Toast.LENGTH_SHORT).show()
			return
		}
		
		val requestFactory = EventsRequestFactory(listOf(group), "measured_at", "DESC", AllAlertsViewModel.PAGE_LIMITS, 0)
		
		eventsUserCase.execute(object : DisposableSingleObserver<EventsResponse>() {
			override fun onSuccess(t: EventsResponse) {
				DownLoadEventWorker.enqueue()
			}
			
			override fun onError(e: Throwable) {
			}
		}, requestFactory)
	}
}