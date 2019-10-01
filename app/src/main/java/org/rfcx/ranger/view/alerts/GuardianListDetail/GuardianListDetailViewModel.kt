package org.rfcx.ranger.view.alerts.GuardianListDetail

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import org.rfcx.ranger.data.remote.Result
import org.rfcx.ranger.data.remote.domain.BaseDisposableSingle
import org.rfcx.ranger.data.remote.groupByGuardians.eventInGuardian.GetEventInGuardian
import org.rfcx.ranger.entity.event.Event
import org.rfcx.ranger.entity.event.EventResponse
import org.rfcx.ranger.entity.event.EventsGuardianRequestFactory

class GuardianListDetailViewModel(private val context: Context, private val getEventInGuardian: GetEventInGuardian) : ViewModel() {
	private val _items = MutableLiveData<Result<ArrayList<MutableList<Event>>>>()
	val items: LiveData<Result<ArrayList<MutableList<Event>>>> get() = _items
	
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
		_items.value = Result.Success(eventAll)
	}
}
