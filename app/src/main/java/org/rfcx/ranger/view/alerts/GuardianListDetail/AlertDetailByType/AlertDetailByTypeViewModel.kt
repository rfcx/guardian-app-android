package org.rfcx.ranger.view.alerts.GuardianListDetail.AlertDetailByType

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import org.rfcx.ranger.data.local.EventDb
import org.rfcx.ranger.data.remote.Result
import org.rfcx.ranger.data.remote.groupByGuardians.eventInGuardian.GetMoreEventInGuardian
import org.rfcx.ranger.entity.event.ReviewEventFactory
import org.rfcx.ranger.util.EventItem
import org.rfcx.ranger.util.replace
import org.rfcx.ranger.util.toEventItem

class AlertDetailByTypeViewModel(private val context: Context, private val eventDb: EventDb, private val getMoreEvent: GetMoreEventInGuardian) : ViewModel() {
	private val _arrayEvent = MutableLiveData<Result<ArrayList<EventItem>>>()      // keep only 50 events
	val arrayEvent: LiveData<Result<ArrayList<EventItem>>> get() = _arrayEvent

	var arrayEventGroupMore = ArrayList<EventItem>() // keep when see older and use updete ui when review
	
	fun getEventFromDatabase(value: String) {
		val events = eventDb.getEvents().filter { it.value == value }
		
		val itemsEvent = arrayListOf<EventItem>()
		events.forEach { event ->
			itemsEvent.add(event.toEventItem(eventDb))
		}
		arrayEventGroupMore = itemsEvent
		_arrayEvent.value = Result.Success(itemsEvent)
	}
	
	fun onEventReviewed(eventGuid: String, reviewValue: String) {
		val updateEventItem = arrayEventGroupMore.firstOrNull { it.event.id == eventGuid }
		if (updateEventItem != null) {
			updateEventItem.state = when (reviewValue) {
				ReviewEventFactory.confirmEvent -> EventItem.State.CONFIRM
				ReviewEventFactory.rejectEvent -> EventItem.State.REJECT
				else -> EventItem.State.NONE
			}
			arrayEventGroupMore.replace(updateEventItem) { it.event.id == eventGuid }
		}
		_arrayEvent.value = Result.Success(arrayEventGroupMore)
	}
}