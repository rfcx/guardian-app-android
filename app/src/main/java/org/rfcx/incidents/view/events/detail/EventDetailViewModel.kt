package org.rfcx.incidents.view.events.detail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.reactivex.observers.DisposableSingleObserver
import org.rfcx.incidents.data.local.AlertDb
import org.rfcx.incidents.data.local.StreamDb
import org.rfcx.incidents.data.local.TrackingDb
import org.rfcx.incidents.data.remote.common.Result
import org.rfcx.incidents.domain.GetEventsUseCase
import org.rfcx.incidents.entity.event.Alert
import org.rfcx.incidents.entity.location.Coordinate
import org.rfcx.incidents.entity.location.Tracking
import org.rfcx.incidents.entity.stream.Stream

class EventDetailViewModel(
    private val alertDb: AlertDb,
    private val streamDb: StreamDb,
    private val trackingDb: TrackingDb,
    private val getEventsUseCase: GetEventsUseCase
) : ViewModel() {
    private val _alerts = MutableLiveData<Result<List<Alert>>>()
    val getAlertsFromRemote: LiveData<Result<List<Alert>>> get() = _alerts

    fun getEventsCount(streamId: String): Long = alertDb.getAlertCount(streamId)

    fun getStream(serverId: String): Stream? = streamDb.getStream(serverId)

    fun getAlertsByStream(streamId: String): List<Alert> = alertDb.getAlertsByDescending(streamId)

    fun saveLocation(tracking: Tracking, coordinate: Coordinate) {
        trackingDb.insertOrUpdate(tracking, coordinate)
    }

    fun fetchEvents(streamId: String) {
        _alerts.value = Result.Loading

        getEventsUseCase.execute(
            object : DisposableSingleObserver<List<Alert>>() {
                override fun onSuccess(t: List<Alert>) {
                    t.forEach { res ->
                        alertDb.insertAlert(res)
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
