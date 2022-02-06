package org.rfcx.incidents.view.events.detail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.reactivex.observers.DisposableSingleObserver
import org.rfcx.incidents.data.local.EventDb
import org.rfcx.incidents.data.local.StreamDb
import org.rfcx.incidents.data.local.TrackingDb
import org.rfcx.incidents.data.remote.common.Result
import org.rfcx.incidents.domain.GetEventsUseCase
import org.rfcx.incidents.entity.event.Event
import org.rfcx.incidents.entity.location.Coordinate
import org.rfcx.incidents.entity.location.Tracking
import org.rfcx.incidents.entity.stream.Stream

class EventDetailViewModel(
    private val eventDb: EventDb,
    private val streamDb: StreamDb,
    private val trackingDb: TrackingDb,
    private val getEventsUseCase: GetEventsUseCase
) : ViewModel() {
    private val _alerts = MutableLiveData<Result<List<Event>>>()
    val getAlertsFromRemote: LiveData<Result<List<Event>>> get() = _alerts

    fun getEventsCount(streamId: String): Long = eventDb.getEventCount(streamId)

    fun getStream(serverId: String): Stream? = streamDb.getStream(serverId)

    fun getAlertsByStream(streamId: String): List<Event> = eventDb.getEventsByDescending(streamId)

    fun saveLocation(tracking: Tracking, coordinate: Coordinate) {
        trackingDb.insertOrUpdate(tracking, coordinate)
    }

    fun fetchEvents(streamId: String) {
        _alerts.value = Result.Loading

        getEventsUseCase.execute(
            object : DisposableSingleObserver<List<Event>>() {
                override fun onSuccess(t: List<Event>) {
                    t.forEach { res ->
                        eventDb.insertEvent(res)
                    }
                    _alerts.value = Result.Success(t)
                }

                override fun onError(e: Throwable) {
                    _alerts.value = Result.Error(e)
                }
            },
            streamId
        )
    }
}
