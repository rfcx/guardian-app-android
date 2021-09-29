package org.rfcx.ranger.view.events.detail

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import org.rfcx.ranger.data.local.EventDb
import org.rfcx.ranger.entity.event.Event
import org.rfcx.ranger.util.asLiveData

class GuardianEventDetailViewModel(private val eventDb: EventDb) : ViewModel() {
	fun getEvents(): LiveData<List<Event>> {
		return Transformations.map(eventDb.getAllResultsAsync().asLiveData()) { it }
	}
}
