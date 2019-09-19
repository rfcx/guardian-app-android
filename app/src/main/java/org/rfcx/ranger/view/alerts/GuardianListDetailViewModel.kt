package org.rfcx.ranger.view.alerts

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import org.rfcx.ranger.data.remote.Result
import org.rfcx.ranger.data.remote.domain.BaseDisposableSingle
import org.rfcx.ranger.data.remote.groupByGuardians.eventInGuardian.GetEventInGuardian
import org.rfcx.ranger.entity.event.Event
import org.rfcx.ranger.entity.event.EventInGuardianResponse

class GuardianListDetailViewModel(private val getEventInGuardian: GetEventInGuardian) : ViewModel() {
	private val _items = MutableLiveData<Result<EventInGuardianResponse>>()
	val items: LiveData<Result<EventInGuardianResponse>> get() = _items
	
	private var eventOfAmazon: MutableList<Event> = mutableListOf()
	private var eventOfMacaw: MutableList<Event> = mutableListOf()
	private var eventOfChainsaw: MutableList<Event> = mutableListOf()
	private var eventOfVehicle: MutableList<Event> = mutableListOf()
	private var eventOfGunshot: MutableList<Event> = mutableListOf()
	private var eventOfTrespasser: MutableList<Event> = mutableListOf()
	private var eventOfOther: MutableList<Event> = mutableListOf()
	private var eventOfElse: MutableList<Event> = mutableListOf()
	
	init {
		loadEvantsGuardian()
	}
	
	private fun loadEvantsGuardian() {
		_items.value = Result.Loading
		getEventInGuardian.execute(GetEventInGuardianDisposable(_items), "5c981e56ac48")
	}
	
	fun makeGroupOfValue(events: List<Event>) {
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
				else -> eventOfElse.add(event)
			}
		}
	}
}

class GetEventInGuardianDisposable(
		private val liveData: MutableLiveData<Result<EventInGuardianResponse>>)
	: BaseDisposableSingle<EventInGuardianResponse>() {
	override fun onSuccess(success: Result<EventInGuardianResponse>) {
		Log.d("success", success.toString())
		liveData.value = success
	}
	
	override fun onError(e: Throwable, error: Result<EventInGuardianResponse>) {
		Log.d("error", error.toString())
		liveData.value = error
	}
}
