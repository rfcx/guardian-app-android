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
import org.rfcx.ranger.entity.guardian.GroupByGuardiansResponse
import org.rfcx.ranger.service.DownLoadEventWorker
import org.rfcx.ranger.util.getGuardianGroup
import org.rfcx.ranger.util.getResultError
import org.rfcx.ranger.util.replace
import org.rfcx.ranger.view.alerts.adapter.EventItem
import kotlin.math.ceil

class AllAlertsViewModel(private val context: Context, private val eventsUserCase: GetEventsUseCase,
                         private val eventDb: EventDb) : ViewModel() {
	
	private val _groupByGuardians = MutableLiveData<Result<GroupByGuardiansResponse>>()
	val groupByGuardians: LiveData<Result<GroupByGuardiansResponse>> get() = _groupByGuardians
	
	private var _alerts = MutableLiveData<Result<List<EventItem>>>()
	val alerts: LiveData<Result<List<EventItem>>>
		get() = _alerts
	
	private var _alertsList: List<EventItem> = listOf()
	private val listGuardians = ArrayList<String>()
	
	// data loading events
	private val items = arrayListOf<EventItem>()
	private var currentOffset: Int = 0
	private var totalItemCount: Int = 0
	private val totalPage: Int
		get() = ceil(totalItemCount.toFloat() / PAGE_LIMITS).toInt()
	private val nextOffset: Int
		get() {
			currentOffset += PAGE_LIMITS
			return currentOffset
		}
	val isLastPage: Boolean
		get() = currentOffset >= totalPage
	
	init {
		currentOffset = 0
		
	}
	
	fun getGuardianGroup(){
		_alerts.value = Result.Loading
		_groupByGuardians.value = Result.Loading
		
		getEventsCache()
		
		val group = context.getGuardianGroup()
		if (group == null) {
			Toast.makeText(context, context.getString(R.string.error_no_guardian_group_set), Toast.LENGTH_SHORT).show()
			return
		}
		val requestFactory = EventsRequestFactory(listOf(group), "measured_at", "DESC", PAGE_LIMITS, 0)
		
		eventsUserCase.execute(object : DisposableSingleObserver<EventResponse>() {
			override fun onSuccess(t: EventResponse) {
				DownLoadEventWorker.enqueue()
				totalItemCount = t.total
				handleOnSuccess(t)
			}
			
			override fun onError(e: Throwable) {
				_alerts.value = e.getResultError()
				
			}
		}, requestFactory)
	}
	
	private fun handleOnSuccess(t: EventResponse) {
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
	
	fun loadMoreEvents() {
		if (isLastPage) {
			return
		}
		
		_alerts.value = Result.Loading
		
		val group = context.getGuardianGroup()
		if (group == null) {
			Toast.makeText(context, context.getString(R.string.error_no_guardian_group_set), Toast.LENGTH_SHORT).show()
			return
		}
		
		val requestFactory = EventsRequestFactory(listGuardians, "begins_at", "DESC", PAGE_LIMITS, nextOffset)
		eventsUserCase.execute(object : DisposableSingleObserver<EventResponse>() {
			override fun onSuccess(t: EventResponse) {
				totalItemCount = t.total
				handleOnSuccess(t)
			}

			override fun onError(e: Throwable) {
				currentOffset -= PAGE_LIMITS
				_alerts.value = e.getResultError()
			}
		}, requestFactory)
	}
	
	private fun getEventsCache() {
		val cacheEvents = eventDb.getEvents()
		val items: List<EventItem> = cacheEvents.map { event ->
			val state = eventDb.getEventState(event.event_guid)
			state?.let {
				val result = when (it) {
					ReviewEventFactory.confirmEvent -> EventItem.State.CONFIRM
					ReviewEventFactory.rejectEvent -> EventItem.State.REJECT
					else -> EventItem.State.NONE
				}
				EventItem(event, result)
			} ?: run {
				(EventItem(event, EventItem.State.NONE))
			}
		}
		if (items.isNotEmpty()) {
			_alertsList = items
			_alerts.value = Result.Success(items)
		}
		
	}
	
	fun onEventReviewed(eventGuid: String, reviewValue: String) {
		val eventItem = _alertsList.firstOrNull { it.event.event_guid == eventGuid }
		if (eventItem != null) {
			eventItem.state = when (reviewValue) {
				ReviewEventFactory.confirmEvent -> EventItem.State.CONFIRM
				ReviewEventFactory.rejectEvent -> EventItem.State.REJECT
				else -> EventItem.State.NONE
			}
			_alertsList.replace(eventItem) { it.event.event_guid == eventGuid }
		}
		_alerts.value = Result.Success(_alertsList)
	}
	
	companion object {
		const val PAGE_LIMITS = 50
	}
}