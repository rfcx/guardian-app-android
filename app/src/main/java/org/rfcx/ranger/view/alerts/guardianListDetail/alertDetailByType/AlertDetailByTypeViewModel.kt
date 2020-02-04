package org.rfcx.ranger.view.alerts.guardianListDetail.alertDetailByType

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.reactivex.observers.DisposableSingleObserver
import org.rfcx.ranger.R
import org.rfcx.ranger.data.local.EventDb
import org.rfcx.ranger.data.remote.Result
import org.rfcx.ranger.data.remote.domain.alert.GetEventUseCase
import org.rfcx.ranger.data.remote.groupByGuardians.eventInGuardian.GetMoreEventInGuardian
import org.rfcx.ranger.entity.event.Event
import org.rfcx.ranger.entity.event.EventsGuardianRequestFactory
import org.rfcx.ranger.entity.event.EventsResponse
import org.rfcx.ranger.entity.event.ReviewEventFactory
import org.rfcx.ranger.util.*

class AlertDetailByTypeViewModel(private val context: Context,
                                 private val eventDb: EventDb,
                                 private val eventUseCase: GetEventUseCase,
                                 private val getMoreEvent: GetMoreEventInGuardian) : ViewModel() {
	private val _arrayEvent = MutableLiveData<Result<EventGroupByValue>>()      // keep only 50 events
	val arrayEvent: LiveData<Result<EventGroupByValue>> get() = _arrayEvent
	
	var arrayEventGroupMore = EventGroupByValue(ArrayList(), EventGroupByValue.StateSeeOlder.DEFAULT) // keep when see older and use updete ui when review
	
	fun getEventFromDatabase(value: String, guardianName: String) {
		val eventsFirstTime = eventDb.getEvents().filter { it.guardianName == guardianName }
		val events = eventsFirstTime.filter { it.value == value }
		
		val itemsEvent = arrayListOf<EventItem>()
		events.forEach { event ->
			itemsEvent.add(event.toEventItem(eventDb))
		}
		arrayEventGroupMore = EventGroupByValue(itemsEvent, EventGroupByValue.StateSeeOlder.DEFAULT)
		_arrayEvent.value = Result.Success(EventGroupByValue(itemsEvent, EventGroupByValue.StateSeeOlder.DEFAULT))
	}
	
	fun onEventReviewed(event: Event, reviewValue: String) {
		val eventItem = arrayEventGroupMore.events.firstOrNull { it.event.id == event.id }
		
		eventItem?.let {
			eventItem.state = when (reviewValue) {
				ReviewEventFactory.confirmEvent -> EventItem.State.CONFIRM
				ReviewEventFactory.rejectEvent -> EventItem.State.REJECT
				else -> EventItem.State.NONE
			}
			
			getEventDetail(eventItem)
			
		} ?: run {
			_arrayEvent.value = Result.Success(arrayEventGroupMore)
		}
	}
	
	private fun getEventDetail(eventItem: EventItem) {
		_arrayEvent.value = Result.Loading
		val eventId = eventItem.event.id
		eventUseCase.execute(object : DisposableSingleObserver<Event>() {
			override fun onSuccess(event: Event) {
				updateEventItem(eventItem, event)
			}
			
			override fun onError(e: Throwable) {
				// just need update view
				updateEventItem(eventItem, eventItem.event)
			}
		}, eventId)
	}
	
	private fun updateEventItem(eventItem: EventItem, newEvent: Event) {
		eventItem.event = newEvent // set new event
		
		arrayEventGroupMore.events.replace(eventItem) { it.event.id == newEvent.id }
		_arrayEvent.value = Result.Success(arrayEventGroupMore)
	}
	
	fun loadMoreEvents() {
		val lastEvent = arrayEventGroupMore.events[arrayEventGroupMore.events.size - 1].event
		val guid = lastEvent.guardianId
		val value = lastEvent.value
		
		_arrayEvent.value = Result.Success(EventGroupByValue(arrayEventGroupMore.events, EventGroupByValue.StateSeeOlder.LOADING))
		
		val requestFactory = EventsGuardianRequestFactory(guid, value, "measured_at", "DESC", LIMITS, arrayEventGroupMore.events.size)
		getMoreEvent.execute(object : DisposableSingleObserver<List<Event>>() {
			override fun onSuccess(events: List<Event>) {
				if (events.isEmpty()) {
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

