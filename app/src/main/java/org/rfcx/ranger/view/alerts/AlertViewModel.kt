package org.rfcx.ranger.view.alerts

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import org.rfcx.ranger.entity.event.Event

class AlertViewModel : ViewModel() {
	
	val eventFromNotification = MutableLiveData<Event>()
	
}