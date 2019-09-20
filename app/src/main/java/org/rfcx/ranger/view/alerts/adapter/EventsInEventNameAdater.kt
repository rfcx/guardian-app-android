package org.rfcx.ranger.view.alerts.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_events_in_event_name.view.*
import org.rfcx.ranger.R
import org.rfcx.ranger.entity.event.Event
import org.rfcx.ranger.util.timeAgoDisplay

class EventsInEventNameAdater(private val items: MutableList<Event>) : RecyclerView.Adapter<EventsInEventNameAdater.EventsInEventNameViewHolder>() {
	
	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventsInEventNameViewHolder {
		val view = LayoutInflater.from(parent.context).inflate(R.layout.item_events_in_event_name, parent, false)
		return EventsInEventNameViewHolder(view)
	}
	
	override fun getItemCount(): Int = items.size
	
	override fun onBindViewHolder(holder: EventsInEventNameViewHolder, position: Int) {
		holder.bind(items[position])
	}
	
	inner class EventsInEventNameViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
		private val eventsInEventsTextView = itemView.eventsInEventsTextView
		private val circleImageView = itemView.circleImageView
		
		var currentEvent: Event? = null
		
		init {
			itemView.setOnClickListener {
				// TODO open AlertBottomDialog
			}
		}
		
		fun bind(event: Event) {
			circleImageView.visibility = View.VISIBLE
			eventsInEventsTextView.text = event.timeAgoDisplay(itemView.context)
			this.currentEvent = event
		}
	}
}
