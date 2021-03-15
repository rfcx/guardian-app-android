package org.rfcx.ranger.view.alerts.guardian.alertType

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.reactivex.observers.DisposableSingleObserver
import org.rfcx.ranger.R
import org.rfcx.ranger.adapter.entity.BaseItem
import org.rfcx.ranger.data.local.EventDb
import org.rfcx.ranger.data.remote.Result
import org.rfcx.ranger.data.remote.groupByGuardians.eventInGuardian.GetMoreEventInGuardian
import org.rfcx.ranger.entity.event.Event
import org.rfcx.ranger.entity.event.EventsGuardianRequestFactory
import org.rfcx.ranger.entity.event.ReviewEventFactory
import org.rfcx.ranger.util.EventItem
import org.rfcx.ranger.util.getResultError
import org.rfcx.ranger.util.replace
import org.rfcx.ranger.util.toEventItem

class AlertValueViewModel(private val context: Context,
                                 private val eventDb: EventDb,
                                 private val getMoreEvent: GetMoreEventInGuardian) : ViewModel() {
	private val _baseItems = MutableLiveData<Result<List<BaseItem>>>()
	val baseItems: LiveData<Result<List<BaseItem>>> get() = _baseItems
	
	private var _alertsList= arrayListOf<EventItem>()
	var isLoadMore = false
	
	fun getEvents(value: String?, guardianName: String) {
		_alertsList.clear()
		val eventsFirstTime = eventDb.getEvents().filter { it.guardianName == guardianName }
		if(value != null) {
			val events = eventsFirstTime.filter { it.value == value }
			addEvents(events)
		} else {
			addEvents(eventsFirstTime)
		}

		_baseItems.value = Result.Success(buildBaseItems(_alertsList, LoadMoreItem.DEFAULT))
	}
	
	private fun addEvents(events: List<Event>) {
		events.forEach { event ->
			val state = eventDb.getEventState(event.id)
			_alertsList.add(event.toEventItem(state))
		}
	}
	
	fun onEventReviewed(newEvent: Event, reviewValue: String) {
		val eventItem = _alertsList.firstOrNull { it.event.id == newEvent.id }
		
		eventItem?.let {
			it.event = newEvent
			it.state = when (reviewValue) {
				ReviewEventFactory.confirmEvent -> EventItem.State.CONFIRM
				ReviewEventFactory.rejectEvent -> EventItem.State.REJECT
				else -> EventItem.State.NONE
			}
			_alertsList.replace(eventItem) {it2 -> it2.event.id == newEvent.id }
		}
		
		_baseItems.value = Result.Success(buildBaseItems(_alertsList, LoadMoreItem.DEFAULT))
	}
	
	fun loadMoreEvents() {
		isLoadMore = true
		loadEvents()
	}
	
	private fun loadEvents() {
		val lastEvent = _alertsList.last().event
		
		// loadmore - show loading
		_baseItems.value = Result.Success(buildBaseItems(_alertsList, LoadMoreItem.LOADING))
		
		val requestFactory = EventsGuardianRequestFactory(lastEvent.guardianId, lastEvent.value,
				"measured_at", "DESC", LIMITS, _alertsList.size)
		getMoreEvent.execute(object : DisposableSingleObserver<List<Event>>() {
			override fun onSuccess(events: List<Event>) {
				isLoadMore = false
				if (events.isEmpty()) {
					Toast.makeText(context, context.getString(R.string.not_have_event_more), Toast.LENGTH_SHORT).show()
					_baseItems.value = Result.Success(buildBaseItems(_alertsList, LoadMoreItem.NOT_FOUND))
				} else {
					events.forEach {
						_alertsList.add(it.toEventItem("none")) // create event item state none
					}
					_baseItems.value = Result.Success(buildBaseItems(_alertsList, LoadMoreItem.DEFAULT))
				}
			}
			
			override fun onError(e: Throwable) {
				isLoadMore = false
				_baseItems.value = e.getResultError()
			}
		}, requestFactory)
	}
	
	private fun buildBaseItems(eventItems: ArrayList<EventItem>, loadMoreItem: LoadMoreItem): ArrayList<BaseItem> {
		return arrayListOf<BaseItem>().apply {
			addAll(eventItems)
			add(loadMoreItem)
		}
	}
	
	companion object {
		const val LIMITS = 10
	}
}

enum class LoadMoreItem : BaseItem {
	LOADING, NOT_FOUND, DEFAULT
}

