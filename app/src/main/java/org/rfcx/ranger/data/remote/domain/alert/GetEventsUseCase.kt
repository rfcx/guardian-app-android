package org.rfcx.ranger.data.remote.domain.alert

import android.util.Log
import io.reactivex.Single
import io.reactivex.observers.DisposableSingleObserver
import org.rfcx.ranger.data.local.CachedEndpointDb
import org.rfcx.ranger.data.local.EventDb
import org.rfcx.ranger.data.remote.ResponseCallback
import org.rfcx.ranger.data.remote.data.alert.EventRepository
import org.rfcx.ranger.data.remote.domain.SingleUseCase
import org.rfcx.ranger.data.remote.domain.executor.PostExecutionThread
import org.rfcx.ranger.data.remote.domain.executor.ThreadExecutor
import org.rfcx.ranger.entity.event.Event
import org.rfcx.ranger.entity.event.EventsRequestFactory
import org.rfcx.ranger.entity.event.EventsResponse
import org.rfcx.ranger.service.DownLoadEventWorker
import org.rfcx.ranger.util.Preferences
import org.rfcx.ranger.view.alerts.AllAlertsViewModel

class GetEventsUseCase(private val eventRepository: EventRepository,
                       private val cachedEndpointDb: CachedEndpointDb,
                       private val eventDb: EventDb,
                       private val pref: Preferences,
                       threadExecutor: ThreadExecutor,
                       postExecutionThread: PostExecutionThread) :
		SingleUseCase<EventsRequestFactory, EventsResponse>(threadExecutor, postExecutionThread) {
	override fun buildUseCaseObservable(params: EventsRequestFactory): Single<EventsResponse> {
		return eventRepository.getRemoteEventList(params)
	}
	
	fun execute(callback: ResponseCallback<Pair<List<Event>, Int>>, params: EventsRequestFactory,
	            force: Boolean = false) {
		
		val endpoint = "v2/events/?guardian_groups[]=${params.guardianInGroup}" +
				"&order=${params.order}&dir=${params.dir}" +
				"&limit=${params.limit}&offset=${params.offset}"
		val isStarting = params.offset == 0 && params.limit == AllAlertsViewModel.PAGE_LIMITS
		
		if (!force && cachedEndpointDb.hasCachedEndpoint(endpoint, 0.05)) {
			Log.d("GetEventsUseCase", "$endpoint -> used cached!")
			val events = eventDb.getEvents()
			var total = pref.getInt(Preferences.EVENT_ONLINE_TOTAL, 0)
			if (total == 0) {
				total = events.size
			}
			callback.onSuccess(Pair(events, total))
			return
		}
		
		this.execute(object : DisposableSingleObserver<EventsResponse>() {
			override fun onSuccess(t: EventsResponse) {
				// cache events from sever
				val total = t.total
				val events = t.events?.map { it.toEvent() } ?: listOf()
				pref.putInt(Preferences.EVENT_ONLINE_TOTAL, total)
				if (events.isNotEmpty()) {
					if (isStarting) {
						val eventCached = eventDb.getEvents()
						var r = listOf<Event>()
						if (events.isNotEmpty() && eventCached.isNotEmpty()) {
							r = events.filter {
								eventCached.firstOrNull { cached ->
									cached.id == it.id
											&& cached.rejectedCount == it.rejectedCount
											&& cached.confirmedCount == it.confirmedCount
								} == null // new event?
							}
						}
						// has new event?
						if (r.isNotEmpty()) {
							Log.d("GetEventsUseCase", "clear events!")
							eventDb.deleteAllEvents()
						}
					}
					// store events
					eventDb.saveEvents(events)
					// start worker download event for offline
					DownLoadEventWorker.enqueue()
				}
				
				// cache endpoint
				if (isStarting && t.events != null && t.events!!.isNotEmpty()) {
					Log.d("GetEventsUseCase", "$endpoint -> cached endpoint!")
					cachedEndpointDb.updateCachedEndpoint(endpoint)
				}
				callback.onSuccess(Pair(events, total))
			}
			
			override fun onError(e: Throwable) {
				callback.onError(e)
			}
			
		}, params)
	}
}