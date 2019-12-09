package org.rfcx.ranger.view.alerts.GuardianListDetail.AlertDetailByType

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.reactivex.observers.DisposableSingleObserver
import org.rfcx.ranger.R
import org.rfcx.ranger.data.local.EventDb
import org.rfcx.ranger.data.remote.Result
import org.rfcx.ranger.data.remote.groupByGuardians.eventInGuardian.GetMoreEventInGuardian
import org.rfcx.ranger.entity.event.EventsGuardianRequestFactory
import org.rfcx.ranger.entity.event.EventsResponse
import org.rfcx.ranger.entity.event.ReviewEventFactory
import org.rfcx.ranger.util.EventItem
import org.rfcx.ranger.util.getResultError
import org.rfcx.ranger.util.replace
import org.rfcx.ranger.util.toEventItem
import java.util.*
import kotlin.collections.ArrayList

class AlertDetailByTypeViewModel(private val context: Context, private val eventDb: EventDb, private val getMoreEvent: GetMoreEventInGuardian) : ViewModel() {
	private val _arrayEvent = MutableLiveData<Result<EventGroupByValue>>()      // keep only 50 events
	val arrayEvent: LiveData<Result<EventGroupByValue>> get() = _arrayEvent
	
	var arrayEventGroupMore = EventGroupByValue(ArrayList(), EventGroupByValue.StateSeeOlder.DEFAULT) // keep when see older and use updete ui when review
	
	fun getEventFromDatabase(value: String) {
		val events = eventDb.getEvents().filter { it.value == value }
		
		val itemsEvent = arrayListOf<EventItem>()
		events.forEach { event ->
			itemsEvent.add(event.toEventItem(eventDb))
		}
		arrayEventGroupMore = EventGroupByValue(itemsEvent, EventGroupByValue.StateSeeOlder.DEFAULT)
		_arrayEvent.value = Result.Success(EventGroupByValue(itemsEvent, EventGroupByValue.StateSeeOlder.DEFAULT))
	}
	
	fun onEventReviewed(eventGuid: String, reviewValue: String) {
		val updateEventItem = arrayEventGroupMore.events.firstOrNull { it.event.id == eventGuid }
		if (updateEventItem != null) {
			updateEventItem.state = when (reviewValue) {
				ReviewEventFactory.confirmEvent -> EventItem.State.CONFIRM
				ReviewEventFactory.rejectEvent -> EventItem.State.REJECT
				else -> EventItem.State.NONE
			}
			arrayEventGroupMore.events.replace(updateEventItem) { it.event.id == eventGuid }
		}
		_arrayEvent.value = Result.Success(arrayEventGroupMore)
	}
	
	fun loadMoreEvents() {
		val lastEvent = arrayEventGroupMore.events[arrayEventGroupMore.events.size - 1].event
		val guid = lastEvent.guardianId
		val value = lastEvent.value
		val beginsAt = lastEvent.beginsAt.time
		val audioDuration = lastEvent.audioDuration
		val timeEndAt = Date(beginsAt + audioDuration)
		
		_arrayEvent.value = Result.Success(EventGroupByValue(arrayEventGroupMore.events, EventGroupByValue.StateSeeOlder.LOADING))
		
		val requestFactory = EventsGuardianRequestFactory(guid, value, timeEndAt, "measured_at", "DESC", LIMITS, 1)
		getMoreEvent.execute(object : DisposableSingleObserver<EventsResponse>() {
			override fun onSuccess(t: EventsResponse) {
				val events = t.events?.map { it.toEvent() } ?: listOf()
				if (t.events!!.isEmpty()) {
					Toast.makeText(context, context.getString(R.string.not_have_event_more), Toast.LENGTH_SHORT).show()
					_arrayEvent.value = Result.Success(EventGroupByValue(arrayEventGroupMore.events, EventGroupByValue.StateSeeOlder.NOT_HAVE_ALERT))
					
				} else {
					var index = arrayEventGroupMore.events.size
					events.forEach {
						arrayEventGroupMore.events.add(index, it.toEventItem(eventDb))
						index += 1
					}
					_arrayEvent.value = Result.Success(EventGroupByValue(arrayEventGroupMore.events, EventGroupByValue.StateSeeOlder.HAVE_ALERTS))
				}
			}
			
			override fun onError(e: Throwable) {
				_arrayEvent.value = e.getResultError()
			}
		}, requestFactory)
	}
	
	companion object {
		const val LIMITS = 10
	}
}

data class EventGroupByValue(val events: ArrayList<EventItem>, var stateSeeOlder: StateSeeOlder) {
	enum class StateSeeOlder {
		LOADING, HAVE_ALERTS, NOT_HAVE_ALERT, DEFAULT
	}
}

