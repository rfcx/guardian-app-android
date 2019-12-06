package org.rfcx.ranger.view.alerts.GuardianListDetail.AlertDetailByType

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_events_in_event_name.view.*
import org.rfcx.ranger.R
import org.rfcx.ranger.util.EventItem
import org.rfcx.ranger.view.alerts.adapter.AlertClickListener

class AlertDetailByTypeAdapter(val listener: AlertClickListener) : ListAdapter<EventItem, AlertDetailByTypeAdapter.AlertDetailByTypeViewHolder>(AlertDetailByTypeDiffUtil()) {
	var items: MutableList<EventItem> = arrayListOf()
		set(value) {
			field = value
			notifyDataSetChanged()
		}
	
	var mOnSeeOlderClickListener: OnSeeOlderClickListener? = null
	
	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlertDetailByTypeViewHolder {
		val view = LayoutInflater.from(parent.context).inflate(R.layout.item_events_in_event_name, parent, false)
		return AlertDetailByTypeViewHolder(view)
	}
	
	override fun getItemCount(): Int = 1
	
	override fun onBindViewHolder(holder: AlertDetailByTypeViewHolder, position: Int) {
		val item = items[position]
		holder.bind()
		holder.itemView.setOnClickListener { listener.onClickedAlert(item.event) }
	}
	
	class AlertDetailByTypeDiffUtil : DiffUtil.ItemCallback<EventItem>() {
		override fun areItemsTheSame(oldItem: EventItem, newItem: EventItem): Boolean {
			return oldItem.state == newItem.state
			
		}
		
		override fun areContentsTheSame(oldItem: EventItem, newItem: EventItem): Boolean {
			return oldItem.event.id == newItem.event.id
					&& oldItem.event.value == newItem.event.value
					&& oldItem.state == newItem.state
		}
	}
	
	inner class AlertDetailByTypeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
		private val itemEventsInEventNameRecycler = itemView.itemEventsInEventNameRecycler
		private val seeOlderTextView = itemView.seeOlderTextView
		private val progressBar = itemView.progressBar
		
		fun bind() {
			itemEventsInEventNameRecycler.apply {
				layoutManager = LinearLayoutManager(context)
				adapter = ItemAlertDetailByTypeAdapter(items, listener)
			}
			
			seeOlderTextView.setOnClickListener {
				mOnSeeOlderClickListener?.onSeeOlderClick()
			}
		}
	}
}