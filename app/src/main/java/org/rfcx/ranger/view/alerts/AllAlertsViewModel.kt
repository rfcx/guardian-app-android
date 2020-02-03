package org.rfcx.ranger.view.alerts

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.*
import io.realm.RealmResults
import org.rfcx.ranger.R
import org.rfcx.ranger.adapter.entity.BaseItem
import org.rfcx.ranger.data.local.EventDb
import org.rfcx.ranger.data.local.GuardianGroupDb
import org.rfcx.ranger.data.local.ProfileData
import org.rfcx.ranger.data.remote.ResponseCallback
import org.rfcx.ranger.data.remote.Result
import org.rfcx.ranger.data.remote.domain.alert.GetEventsUseCase
import org.rfcx.ranger.entity.event.Event
import org.rfcx.ranger.entity.event.EventsRequestFactory
import org.rfcx.ranger.entity.event.ReviewEventFactory
import org.rfcx.ranger.entity.guardian.GroupByGuardiansResponse
import org.rfcx.ranger.util.*
import org.rfcx.ranger.view.alerts.adapter.LoadingItem

class AllAlertsViewModel(private val context: Context,
                         private val eventsUserCase: GetEventsUseCase,
                         private val eventDb: EventDb,
                         private val profileData: ProfileData,
                         pref: Preferences) : ViewModel() {
	
	private val _groupByGuardians = MutableLiveData<Result<GroupByGuardiansResponse>>()
	val groupByGuardians: LiveData<Result<GroupByGuardiansResponse>> get() = _groupByGuardians
	
	private lateinit var eventLiveData: LiveData<List<Event>>
	private var _alerts = MutableLiveData<Result<List<EventItem>>>()
	val alerts: LiveData<Result<List<EventItem>>>
		get() = _alerts
	private var _alertsList: List<EventItem> = listOf()
	
	// data loading events
	private val items = arrayListOf<EventItem>()
	private var currentOffset: Int = 0
	private var totalItemCount: Int = 0
	var isLoadMore = false
	
	private val eventObserve = Observer<List<Event>> {
		if (it.isNotEmpty()) {
			val cacheEvents = eventDb.getEvents()
			this.currentOffset = cacheEvents.size
			handleAlerts(events = cacheEvents)
		}
	}
	
	init {
		_alerts.value = Result.Loading
		currentOffset = 0
		totalItemCount = pref.getInt(Preferences.EVENT_ONLINE_TOTAL, 0)
		fetchEvents()
	}
	
	fun refresh() {
		currentOffset = 0
		totalItemCount = 0
		isLoadMore = false
		_alertsList = listOf()
		items.clear()
		loadEvents()
	}
	
	private fun fetchEvents() {
		eventLiveData = Transformations.map<RealmResults<Event>,
				List<Event>>(eventDb.getAllResultsAsync().asLiveData()) {
			it
		}
		eventLiveData.observeForever(eventObserve)
	}
	
	private fun loadEvents() {
		_alerts.value = Result.Loading
		
		val group = profileData.getGuardianGroup()
		if (group == null) {
			Toast.makeText(context, context.getString(R.string.error_no_guardian_group_set), Toast.LENGTH_SHORT).show()
			return
		}
		
		val requestFactory = EventsRequestFactory(listOf(group.shortname), "measured_at", "DESC",
				PAGE_LIMITS, currentOffset, group.values)
		
		eventsUserCase.execute(object : ResponseCallback<Pair<List<Event>, Int>> {
			override fun onSuccess(t: Pair<List<Event>, Int>) {
				totalItemCount = t.second
				isLoadMore = false
				
				// response is empty?
				if (t.first.isEmpty()) {
					_alerts.value = Result.Success(items)
				}
			}
			
			override fun onError(e: Throwable) {
				currentOffset -= PAGE_LIMITS
				_alerts.value = e.getResultError()
				isLoadMore = false
			}
		}, requestFactory, true)
	}
	
	fun loadMoreEvents() {
		isLoadMore = true
		loadEvents()
	}
	
	private fun handleAlerts(events: List<Event>) {
		this.items.clear()
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
	
	fun onEventReviewed(reviewValue: String, event: Event) {
		val eventItem = _alertsList.firstOrNull { it.event.id == event.id }
		if (eventItem != null) {
			eventItem.state = when (reviewValue) {
				ReviewEventFactory.confirmEvent -> EventItem.State.CONFIRM
				ReviewEventFactory.rejectEvent -> EventItem.State.REJECT
				else -> EventItem.State.NONE
			}
			
			if (reviewValue == ReviewEventFactory.confirmEvent) {
				eventItem.event.confirmedCount = event.confirmedCount + 1
				if (event.firstNameReviewer == context.getNameEmail()) {
					eventItem.event.rejectedCount = event.rejectedCount - 1
				} else {
					eventItem.event.rejectedCount = event.rejectedCount
				}
			} else if (reviewValue == ReviewEventFactory.rejectEvent) {
				eventItem.event.rejectedCount = event.rejectedCount + 1
				if (event.firstNameReviewer == context.getNameEmail()) {
					eventItem.event.confirmedCount = event.confirmedCount - 1
				} else {
					eventItem.event.confirmedCount = event.confirmedCount
				}
			}
			
			_alertsList.replace(eventItem) { it.event.id == event.id }
		}
		_alerts.value = Result.Success(_alertsList)
	}
	
	// Loading more update list
	fun getItemsWithLoading(): List<BaseItem> {
		val listResult = arrayListOf<BaseItem>()
		items.forEach { item -> listResult.add(item.copy()) }
		listResult.add(LoadingItem())
		return listResult
	}
	
	companion object {
		const val PAGE_LIMITS = 50
	}
}