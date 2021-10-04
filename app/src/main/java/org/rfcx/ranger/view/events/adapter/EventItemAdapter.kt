package org.rfcx.ranger.view.events.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_event.view.*
import org.rfcx.ranger.R
import org.rfcx.ranger.entity.alert.Alert
import org.rfcx.ranger.util.toTimeSinceStringAlternativeTimeAgo

class EventItemAdapter : RecyclerView.Adapter<EventItemAdapter.EventItemViewHolder>() {
	var items: List<Alert> = arrayListOf()
		@SuppressLint("NotifyDataSetChanged")
		set(value) {
			field = value
			notifyDataSetChanged()
		}
	
	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventItemViewHolder {
		val view = LayoutInflater.from(parent.context).inflate(R.layout.item_event, parent, false)
		return EventItemViewHolder(view)
	}
	
	override fun getItemCount(): Int = items.size
	
	override fun onBindViewHolder(holder: EventItemViewHolder, position: Int) {
		holder.bind(items[position])
	}
	
	inner class EventItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
		private val typeTextView = itemView.typeTextView
		private val dateTextView = itemView.dateTextView
		
		fun bind(item: Alert) {
			dateTextView.text = item.createdAt.toTimeSinceStringAlternativeTimeAgo(itemView.context)
			typeTextView.text = item.classification?.title
		}
	}
}
