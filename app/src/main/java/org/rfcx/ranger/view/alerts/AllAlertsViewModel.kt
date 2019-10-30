package org.rfcx.ranger.view.alerts

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
import org.rfcx.ranger.data.remote.domain.alert.GetEventsUseCase
import org.rfcx.ranger.entity.event.EventsResponse
import org.rfcx.ranger.entity.event.EventsRequestFactory
import org.rfcx.ranger.entity.event.ReviewEventFactory
import org.rfcx.ranger.entity.guardian.GroupByGuardiansResponse
import org.rfcx.ranger.service.DownLoadEventWorker
import org.rfcx.ranger.util.getGuardianGroup
import org.rfcx.ranger.util.getResultError
import org.rfcx.ranger.util.replace
import org.rfcx.ranger.view.alerts.adapter.EventItem
import org.rfcx.ranger.view.alerts.adapter.LoadingItem
import kotlin.math.ceil

class AllAlertsViewModel(private val context: Context, private val eventsUserCase: GetEventsUseCase,
                         private val eventDb: EventDb) : ViewModel() {
	
	private val _groupByGuardians = MutableLiveData<Result<GroupByGuardiansResponse>>()
	val groupByGuardians: LiveData<Result<GroupByGuardiansResponse>> get() = _groupByGuardians
	
	private var _alerts = MutableLiveData<Result<List<EventItem>>>()
	val alerts: LiveData<Result<List<EventItem>>>
		get() = _alerts
	private var _alertsList: List<EventItem> = listOf()
	
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
	var isLoadMore = false
	val isLastPage: Boolean
		get() = currentOffset >= (PAGE_LIMITS * totalPage)
	
	init {
		currentOffset = 0
	}
	
	fun refresh() {
		currentOffset = 0
		totalItemCount = 0
		isLoadMore = false
		_alertsList = listOf()
		items.clear()
		loadEvents()
	}
	
	fun loadEvents() {
		isLoadMore = false
		_alerts.value = Result.Loading
		
		getEventsCache()
		
		val group = context.getGuardianGroup()
		if (group == null) {
			Toast.makeText(context, context.getString(R.string.error_no_guardian_group_set), Toast.LENGTH_SHORT).show()
			return
		}
		val requestFactory = EventsRequestFactory(listOf(group), "measured_at", "DESC", PAGE_LIMITS, 0)
		
		eventsUserCase.execute(object : DisposableSingleObserver<EventsResponse>() {
			override fun onSuccess(t: EventsResponse) {
				DownLoadEventWorker.enqueue()
				handleOnSuccess(t)
			}
			
			override fun onError(e: Throwable) {
				_alerts.value = e.getResultError()
				
			}
		}, requestFactory)
	}
	
	private fun handleOnSuccess(t: EventsResponse) {
		this.totalItemCount = t.total
		
		val events = t.events?.map { it.toEvent() } ?: listOf()
		events.forEach { event ->
			val state = eventDb.getEventState(event.id)
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
		
		isLoadMore = true
		_alerts.value = Result.Loading
		
		val group = context.getGuardianGroup()
		if (group == null) {
			Toast.makeText(context, context.getString(R.string.error_no_guardian_group_set), Toast.LENGTH_SHORT).show()
			return
		}
		
		val requestFactory = EventsRequestFactory(listOf(group), "measured_at", "DESC", PAGE_LIMITS, nextOffset)
		
		eventsUserCase.execute(object : DisposableSingleObserver<EventsResponse>() {
			override fun onSuccess(t: EventsResponse) {
				totalItemCount = t.total

				handleOnSuccess(t)
				isLoadMore = false
			}

			override fun onError(e: Throwable) {
				currentOffset -= PAGE_LIMITS
				_alerts.value = e.getResultError()
				isLoadMore = false
			}
		}, requestFactory)
	}
	
	private fun getEventsCache() {
		val cacheEvents = eventDb.getEvents()
		val items: List<EventItem> = cacheEvents.map { event ->
			val state = eventDb.getEventState(event.id)
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
		val eventItem = _alertsList.firstOrNull { it.event.id == eventGuid }
		if (eventItem != null) {
			eventItem.state = when (reviewValue) {
				ReviewEventFactory.confirmEvent -> EventItem.State.CONFIRM
				ReviewEventFactory.rejectEvent -> EventItem.State.REJECT
				else -> EventItem.State.NONE
			}
			_alertsList.replace(eventItem) { it.event.id == eventGuid }
		}
		_alerts.value = Result.Success(_alertsList)
	}
	
	// Loading more update list
	fun getItemsWithLoading() : List<BaseItem> {
		val listResult = arrayListOf<BaseItem>()
		items.forEach { item -> listResult.add(item.copy()) }
		listResult.add(LoadingItem())
		return listResult
	}
	
	companion object {
		const val PAGE_LIMITS = 50
	}
}