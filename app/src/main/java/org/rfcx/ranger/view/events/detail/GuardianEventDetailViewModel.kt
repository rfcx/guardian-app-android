package org.rfcx.ranger.view.events.detail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.reactivex.observers.DisposableSingleObserver
import org.rfcx.ranger.data.api.events.GetEvents
import org.rfcx.ranger.data.api.events.ResponseEvent
import org.rfcx.ranger.data.api.events.toAlert
import org.rfcx.ranger.data.remote.Result
import org.rfcx.ranger.entity.alert.Alert

class GuardianEventDetailViewModel(private val getEvents: GetEvents) : ViewModel() {
	private val _events = MutableLiveData<Result<List<Alert>>>()
	val events: LiveData<Result<List<Alert>>> get() = _events
	
	fun loadEvents(streamId: String) {
		getEvents.execute(object : DisposableSingleObserver<List<ResponseEvent>>() {
			override fun onSuccess(t: List<ResponseEvent>) {
				val events = t.map { it.toAlert() }
				_events.value = Result.Success(events)
			}
			
			override fun onError(e: Throwable) {
				_events.value = Result.Error(e)
			}
		}, streamId)
	}
}
