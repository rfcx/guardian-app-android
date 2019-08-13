package org.rfcx.ranger.view.alerts

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import org.rfcx.ranger.entity.event.Event

class AlertsViewModel : ViewModel() {
    private val loading = MutableLiveData<Boolean>()
    private var alerts = MutableLiveData<List<Event>>()

    init {
        // set default loading
        loading.value = true
    }

    fun loadEvents() {
        //TODO: observe load events?
        loading.value = false
    }

    fun getAlerts(): LiveData<List<Event>> = alerts

    fun getLoading(): LiveData<Boolean> = loading

    companion object {
        private const val PAGE_LIMITS = 12
    }
}