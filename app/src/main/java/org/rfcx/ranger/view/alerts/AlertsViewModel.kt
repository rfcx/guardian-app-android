package org.rfcx.ranger.view.alerts

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.reactivex.observers.DisposableSingleObserver
import org.rfcx.ranger.R
import org.rfcx.ranger.adapter.entity.EventItem
import org.rfcx.ranger.data.remote.domain.alert.GetEventsUseCase
import org.rfcx.ranger.entity.event.Event
import org.rfcx.ranger.entity.event.EventResponse
import org.rfcx.ranger.entity.event.EventsRequestFactory
import org.rfcx.ranger.util.RealmHelper
import org.rfcx.ranger.util.getGuardianGroup
import kotlin.math.ceil

class AlertsViewModel(private val context: Context, private val eventsUserCase: GetEventsUseCase) : ViewModel() {
    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean>
        get() = _loading

    private var _alerts = MutableLiveData<List<Event>>()
    val alerts: LiveData<List<Event>>
        get() = _alerts

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

        // set default loading
        _loading.value = false
    }


    fun loadEvents() {
        if (_loading.value == true && isLastPage) {
            return
        }

        // start load
        val group = context.getGuardianGroup()
        if (group == null) {
            Toast.makeText(context, context.getString(R.string.error_no_guardian_group_set), Toast.LENGTH_SHORT).show()
            return
        }

        _loading.value = true

        val requestFactory = EventsRequestFactory(group, "begins_at", "DESC", PAGE_LIMITS, nextOffset)
        eventsUserCase.execute(object : DisposableSingleObserver<EventResponse>() {
            override fun onSuccess(t: EventResponse) {
                _loading.value = false
                totalItemCount = t.total

                t.events?.forEach { event ->
                    // is Read?
                    val localEvent = RealmHelper.getInstance().findLocalEvent(event.event_guid)
                    localEvent?.let {
                        event.isOpened = it.isOpened
                    }

                }
                _alerts.value = t.events
            }

            override fun onError(e: Throwable) {
                _loading.value = false
            }
        }, requestFactory)
    }

    companion object {
        const val PAGE_LIMITS = 50
    }
}