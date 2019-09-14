package org.rfcx.ranger.view.alerts

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.reactivex.observers.DisposableSingleObserver
import org.rfcx.ranger.R
import org.rfcx.ranger.data.local.EventDb
import org.rfcx.ranger.data.remote.Result
import org.rfcx.ranger.data.remote.domain.alert.GetEventsUseCase
import org.rfcx.ranger.entity.event.EventResponse
import org.rfcx.ranger.entity.event.EventsRequestFactory
import org.rfcx.ranger.entity.event.ReviewEventFactory
import org.rfcx.ranger.util.getGuardianGroup
import org.rfcx.ranger.util.getResultError
import org.rfcx.ranger.view.alerts.adapter.EventItem
import kotlin.math.ceil

class AllAlertsViewModel(private val context: Context, private val eventsUserCase: GetEventsUseCase,
                         private val eventDb: EventDb) : ViewModel() {
	
	private var _alerts = MutableLiveData<Result<List<EventItem>>>()
	val alerts: LiveData<Result<List<EventItem>>>
		get() = _alerts
	
	private var _alertsList: List<EventItem> = listOf()
	
	// data loading events
	private var currentOffset: Int = 0
	private var totalItemCount: Int = 0
	private val totalPage: Int
		get() = ceil(totalItemCount.toFloat() / PAGE_LIMITS).toInt()
	private val nextOffset: Int
		get() {
			currentOffset += PAGE_LIMITS
			return currentOffset
		}
	private val isLastPage: Boolean
		get() = currentOffset >= totalPage
	
	init {
		currentOffset = 0
		
	}
	
	
	fun loadEvents() {
		/*if (isLastPage) {
			return
		}*/
		
		_alerts.value = Result.Loading
		
		// start load
		val group = context.getGuardianGroup()
		if (group == null) {
			Toast.makeText(context, context.getString(R.string.error_no_guardian_group_set), Toast.LENGTH_SHORT).show()
			return
		}
		
		val requestFactory = EventsRequestFactory(group, "begins_at", "DESC", PAGE_LIMITS, nextOffset)
		eventsUserCase.execute(object : DisposableSingleObserver<EventResponse>() {
			override fun onSuccess(t: EventResponse) {
				
				totalItemCount = t.total
				val items = arrayListOf<EventItem>()
				t.events?.forEach { event ->
					val state = eventDb.getEventState(event.event_guid)
					state?.let {
						val result = when (it) {
							ReviewEventFactory.confirmEvent -> EventItem.State.CONFIRM
							ReviewEventFactory.rejectEvent -> EventItem.State.REJECT
							else -> EventItem.State.NONE
						}
						items.add(EventItem(event, result))
					} ?: run {
						items.add(EventItem(event, EventItem.State.NONE))
					}
				}
				_alertsList = items
				_alerts.value = Result.Success(items)
			}
			
			override fun onError(e: Throwable) {
				_alerts.value = e.getResultError()
				
			}
		}, requestFactory)
	}
	
	fun onEventReviewed(eventGuid: String, reviewValue: String) {
		val newItems = arrayListOf<EventItem>()
		_alertsList.forEach {
			if (it.event.event_guid == eventGuid) {
				// Update this item
				it.state = when (reviewValue) {
					ReviewEventFactory.confirmEvent -> EventItem.State.CONFIRM
					ReviewEventFactory.rejectEvent -> EventItem.State.REJECT
					else -> EventItem.State.NONE
				}
			}
			newItems.add(EventItem(it.event, it.state))
		}
		_alerts.value = Result.Success(newItems)
	}
	
	companion object {
		const val PAGE_LIMITS = 50
	}
}