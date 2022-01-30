package org.rfcx.incidents.view.events.detail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.reactivex.observers.DisposableSingleObserver
import org.rfcx.incidents.data.api.events.GetEvents
import org.rfcx.incidents.data.api.events.ResponseEvent
import org.rfcx.incidents.data.local.AlertDb
import org.rfcx.incidents.data.remote.Result
import org.rfcx.incidents.entity.Stream
import org.rfcx.incidents.entity.alert.Alert
import org.rfcx.incidents.entity.location.Coordinate
import org.rfcx.incidents.entity.location.Tracking
import org.rfcx.incidents.localdb.StreamDb
import org.rfcx.incidents.localdb.TrackingDb

class GuardianEventDetailViewModel(
    private val alertDb: AlertDb,
    private val streamDb: StreamDb,
    private val trackingDb: TrackingDb,
    private val getEvents: GetEvents
) : ViewModel() {
    private val _alerts = MutableLiveData<Result<List<ResponseEvent>>>()
    val getAlertsFromRemote: LiveData<Result<List<ResponseEvent>>> get() = _alerts
    
    fun getEventsCount(streamId: String): Long = alertDb.getAlertCount(streamId)
    
    fun getStream(serverId: String): Stream? = streamDb.getStreamByCoreId(serverId)
    
    fun getAlertsByStream(streamId: String): List<Alert> = alertDb.getAlertsByDescending(streamId)
    
    fun saveLocation(tracking: Tracking, coordinate: Coordinate) {
        trackingDb.insertOrUpdate(tracking, coordinate)
    }
    
    fun fetchEvents(streamId: String) {
        _alerts.value = Result.Loading
        
        getEvents.execute(object : DisposableSingleObserver<List<ResponseEvent>>() {
            override fun onSuccess(t: List<ResponseEvent>) {
                t.forEach { res ->
                    alertDb.insertAlert(res)
                }
                _alerts.value = Result.Success(t)
            }
            
            override fun onError(e: Throwable) {
                _alerts.value = Result.Error(e)
            }
        }, streamId)
    }
}
