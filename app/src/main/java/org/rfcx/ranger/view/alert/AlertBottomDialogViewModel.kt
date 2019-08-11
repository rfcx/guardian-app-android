package org.rfcx.ranger.view.alert

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import org.rfcx.ranger.entity.event.Event

class AlertBottomDialogViewModel : ViewModel() {
	
	
	private var _event: MutableLiveData<Event> = MutableLiveData()
	val event: LiveData<Event>
		get() = _event
	
	private var _spectrogramImage: MutableLiveData<String> = MutableLiveData()
	val spectrogramImage: LiveData<String>
		get() = _spectrogramImage
	
	private var _eventState: MutableLiveData<EventState> = MutableLiveData()
	val eventState: LiveData<EventState>
		get() = _eventState
	
	fun setEvent(event: Event) {
		this._event.value = event
		setSpectrogramImage()
		this._eventState.value = EventState.NONE
	}
	
	private fun setSpectrogramImage() {
		_spectrogramImage.value = "https://assets.rfcx.org/audio/${event.value?.audioGUID}.png?width=512&height=256" +
				"&offset=${0}&duration=${90L * 1000}"
	}
	
}

enum class EventState {
	NONE, REVIEWED
}