package org.rfcx.ranger.repo

import android.content.Context

import org.rfcx.ranger.entity.event.EventResponse
import org.rfcx.ranger.entity.event.Event
import org.rfcx.ranger.entity.message.Message
import org.rfcx.ranger.repo.api.EventsApi
import org.rfcx.ranger.repo.api.MessageApi


object MessageContentProvider {
	
	fun getMessageAndEvent(context: Context, loadEvent: Boolean, onContentCallBack: OnContentCallBack) {
		var isEventLoaded = false
		var isMessageLoaded = false
		val messagesResponse = ArrayList<Message>()
		val eventsResponse = ArrayList<Event>()
		
		if (loadEvent) {
			EventsApi().getEvents(context, 10, object : EventsApi.OnEventsCallBack {
				override fun onSuccess(event: EventResponse) {
					isEventLoaded = true
					event.events?.let {
						eventsResponse.addAll(it.toTypedArray())
					}
					if (isMessageLoaded)
						onContentCallBack.onContentLoaded(messagesResponse, eventsResponse)
				}
				
				override fun onFailed(t: Throwable?, message: String?) {
					isEventLoaded = true
					onContentCallBack.onFailed(t, message)
					if (isMessageLoaded)
						onContentCallBack.onContentLoaded(messagesResponse, eventsResponse)
				}
				
			})
		} else {
			isEventLoaded = true
		}
		
		
		MessageApi().getMessage(context, object : MessageApi.OnMessageCallBack {
			override fun onSuccess(messages: List<Message>) {
				isMessageLoaded = true
				messagesResponse.addAll(messages)
				if (isEventLoaded)
					onContentCallBack.onContentLoaded(messagesResponse, eventsResponse)
			}
			
			override fun onFailed(t: Throwable?, message: String?) {
				isMessageLoaded = true
				onContentCallBack.onFailed(t, message)
				if (isEventLoaded)
					onContentCallBack.onContentLoaded(messagesResponse, eventsResponse)
			}
		})
	}

	fun getMessage(context: Context, onMessageCallback: OnMessageCallback) {
		MessageApi().getMessage(context, object : MessageApi.OnMessageCallBack {
			override fun onSuccess(messages: List<Message>) {
				onMessageCallback.onMessageLoaded(messages)
			}

			override fun onFailed(t: Throwable?, message: String?) {
				onMessageCallback.onFailed(t, message)
			}
		})
	}

	fun getEvents(context: Context, onEventsCallback: OnEventsCallback) {
		EventsApi().getEvents(context, 10, object: EventsApi.OnEventsCallBack {
			override fun onSuccess(event: EventResponse) {
				val eventsResponse = ArrayList<Event>()
				event.events?.let {
					eventsResponse.addAll(it.toTypedArray())
				}
				onEventsCallback.onEventsLoaded(eventsResponse)
			}

			override fun onFailed(t: Throwable?, message: String?) {
				onEventsCallback.onFailed(t, message)
			}
		})
	}
	
	interface OnContentCallBack : ApiCallback {
		fun onContentLoaded(messages: List<Message>?, events: List<Event>?)
	}

	interface OnMessageCallback : ApiCallback {
		fun onMessageLoaded(messages: List<Message>)
	}

	interface OnEventsCallback : ApiCallback {
		fun onEventsLoaded(events: List<Event>)
	}
}