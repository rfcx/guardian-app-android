package org.rfcx.ranger.view.alerts.GuardianListDetail

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import org.rfcx.ranger.data.local.EventDb
import org.rfcx.ranger.data.remote.Result
import org.rfcx.ranger.entity.event.Event
import org.rfcx.ranger.entity.event.ReviewEventFactory

class GuardianListDetailViewModel(private val context: Context, private val eventDb: EventDb) : ViewModel() {
	private val _items = MutableLiveData<Result<ArrayList<GuardianListDetail>>>()
	val items: LiveData<Result<ArrayList<GuardianListDetail>>> get() = _items
	
	lateinit var value: String
	
	private var eventOfAmazon: MutableList<Event> = mutableListOf()
	private var eventOfMacaw: MutableList<Event> = mutableListOf()
	private var eventOfChainsaw: MutableList<Event> = mutableListOf()
	private var eventOfVehicle: MutableList<Event> = mutableListOf()
	private var eventOfGunshot: MutableList<Event> = mutableListOf()
	private var eventOfTrespasser: MutableList<Event> = mutableListOf()
	private var eventOfOther: MutableList<Event> = mutableListOf()
	private var eventOfMismatch: MutableList<Event> = mutableListOf()
	
	var eventAll: ArrayList<MutableList<Event>> = ArrayList()
	
	fun makeGroupOfValue(events: List<Event>) {
		_items.value = Result.Loading
		events.forEach { event ->
			when {
				event.value == "amazon" -> {
					eventOfAmazon.add(event)
				}
				event.value == "macaw" -> {
					eventOfMacaw.add(event)
				}
				event.value == "chainsaw" -> {
					eventOfChainsaw.add(event)
				}
				event.value == "vehicle" -> {
					eventOfVehicle.add(event)
				}
				event.value == "gunshot" -> {
					eventOfGunshot.add(event)
				}
				event.value == "trespasser" -> {
					eventOfTrespasser.add(event)
				}
				event.value == "other" -> {
					eventOfOther.add(event)
				}
				else -> eventOfMismatch.add(event)
			}
		}
		groupAll()
	}
	
	private fun groupAll() {
		if (eventOfAmazon.isNotEmpty()) {
			eventAll.addAll(listOf(eventOfAmazon))
		}
		
		if (eventOfMacaw.isNotEmpty()) {
			eventAll.addAll(listOf(eventOfMacaw))
		}
		
		if (eventOfChainsaw.isNotEmpty()) {
			eventAll.addAll(listOf(eventOfChainsaw))
		}
		
		if (eventOfVehicle.isNotEmpty()) {
			eventAll.addAll(listOf(eventOfVehicle))
		}
		
		if (eventOfGunshot.isNotEmpty()) {
			eventAll.addAll(listOf(eventOfGunshot))
		}
		
		if (eventOfTrespasser.isNotEmpty()) {
			eventAll.addAll(listOf(eventOfTrespasser))
		}
		
		if (eventOfOther.isNotEmpty()) {
			eventAll.addAll(listOf(eventOfOther))
		}
		
		if (eventOfOther.isNotEmpty()) {
			eventAll.addAll(listOf(eventOfOther))
		}
		addList(eventAll)
	}
	
	private fun numEvents(groupAlert: List<Event>): Int {
		var count = 0
		groupAlert.forEach { event ->
			
			val state = eventDb.getEventState(event.event_guid)
			if (state == ReviewEventFactory.confirmEvent || state == ReviewEventFactory.rejectEvent) {
				count += 1
			}
		}
		return count
	}
	
	private fun addList(array: ArrayList<MutableList<Event>>){
		val arrayList = ArrayList<GuardianListDetail>()
		array.forEach { events ->
			val num = events.size - numEvents(events)
			arrayList.add(GuardianListDetail(events, num))
		}
		_items.value = Result.Success(arrayList)
	}
}

data class GuardianListDetail(val events: MutableList<Event>, val unread: Int)
