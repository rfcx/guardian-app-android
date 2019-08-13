package org.rfcx.ranger.repo

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import org.rfcx.ranger.entity.event.Event
import org.rfcx.ranger.entity.event.EventResponse
import org.rfcx.ranger.repo.api.EventsApi

class AlertRepository(val context: Context) {

    fun getEvents(limit: Int, offset: Int):LiveData<List<Event>> {
        val data = MutableLiveData<List<Event>>()

        EventsApi().getEvents(context, limit, offset, object : EventsApi.OnEventsCallBack {
            override fun onSuccess(event: EventResponse) {
                val eventsResponse = ArrayList<Event>()
                event.events?.let {
                    eventsResponse.addAll(it.toTypedArray())
                }
                data.value = eventsResponse
            }

            override fun onFailed(t: Throwable?, message: String?) {
                //TODO: handle on failed
            }
        })

        return data
    }
}