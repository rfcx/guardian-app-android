package org.rfcx.incidents.view.events.detail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.reactivex.observers.DisposableSingleObserver
import org.rfcx.incidents.data.local.AlertDb
import org.rfcx.incidents.data.local.StreamDb
import org.rfcx.incidents.data.local.TrackingDb
import org.rfcx.incidents.data.remote.common.Result
import org.rfcx.incidents.data.remote.events.ResponseEvent
import org.rfcx.incidents.domain.GetEventsUseCase
import org.rfcx.incidents.entity.Stream
import org.rfcx.incidents.entity.alert.Alert
import org.rfcx.incidents.entity.location.Coordinate
import org.rfcx.incidents.entity.location.Tracking

class EventDetailViewModel(
    private val alertDb: AlertDb,
    private val streamDb: StreamDb,
    private val trackingDb: TrackingDb,
    private val getEventsUseCase: GetEventsUseCase
) : ViewModel() {
    private val _alerts = MutableLiveData<Result<List<ResponseEvent>>>()
    val getAlertsFromRemote: LiveData<Result<List<ResponseEvent>>> get() = _alerts

    fun getEventsCount(streamId: String): Long = alertDb.getAlertCount(streamId)

    fun getStream(serverId: String): Stream? = streamDb.getStream(serverId)

    fun getAlertsByStream(streamId: String): List<Alert> = alertDb.getAlertsByDescending(streamId)

    fun saveLocation(tracking: Tracking, coordinate: Coordinate) {
        trackingDb.insertOrUpdate(tracking, coordinate)
    }

    fun fetchEvents(streamId: String) {
        _alerts.value = Result.Loading

        getEventsUseCase.execute(
            object : DisposableSingleObserver<List<ResponseEvent>>() {
                override fun onSuccess(t: List<ResponseEvent>) {
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
