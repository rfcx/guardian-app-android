package org.rfcx.ranger.view.alerts.guardian

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import io.realm.RealmResults
import org.rfcx.ranger.data.local.EventDb
import org.rfcx.ranger.data.remote.Result
import org.rfcx.ranger.entity.event.Event
import org.rfcx.ranger.entity.event.ReviewEventFactory
import org.rfcx.ranger.util.asLiveData

class GuardianViewModel(private val eventDb: EventDb) : ViewModel() {
	
	var guardianName: String = ""
	
	private lateinit var eventLiveData: LiveData<List<Event>>
	private val _eventGroups = MutableLiveData<Result<ArrayList<EventGroupItem>>>()
	val eventGroups: LiveData<Result<ArrayList<EventGroupItem>>> get() = _eventGroups
	
	private val eventObserve = Observer<List<Event>> {
		if (it.isNotEmpty()) {
			handleEvents(events = it)
		}
	}
	
	fun fetchEventsByGuardianName() {
		eventLiveData = Transformations.map<RealmResults<Event>,
				List<Event>>(eventDb.getByGuardianName(guardianName).asLiveData()) {
			it
		}
		eventLiveData.observeForever(eventObserve)
	}
	
	private fun handleEvents(events: List<Event>) {
		val eventsMap: MutableMap<String, MutableList<Event>> = mutableMapOf()
		
		events.forEach { event ->
			if (!eventsMap.containsKey(event.value)) {
				eventsMap[event.value] = mutableListOf(event)
			} else {
				eventsMap[event.value]?.add(event)
			}
		}
		
		val eventItems = eventsMap.mapTo(arrayListOf(), {
			val read = it.value.fold(0) { acc, event ->
				val state = eventDb.getEventState(event.id)
				if (state == ReviewEventFactory.confirmEvent || state == ReviewEventFactory.rejectEvent) acc + 1 else acc
			}
			val unReviewedCount = it.value.size - read
			EventGroupItem(it.key, it.value[0].label, unReviewedCount)
		})
		
		_eventGroups.value = Result.Success(eventItems)
	}
}

data class EventGroupItem(val value: String, val displayName: String, val unReviewedCount: Int)