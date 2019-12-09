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
	var items: EventGroupByValue = EventGroupByValue(arrayListOf(), EventGroupByValue.StateSeeOlder.DEFAULT)
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
		val item = items.events[position]
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
				adapter = ItemAlertDetailByTypeAdapter(items.events, listener)
			}
			
			seeOlderTextView.setOnClickListener {
				mOnSeeOlderClickListener?.onSeeOlderClick()
			}
			
			when {
				items.stateSeeOlder == EventGroupByValue.StateSeeOlder.LOADING -> {
					progressBar.visibility = View.VISIBLE
					seeOlderTextView.visibility = View.GONE
				}
				items.stateSeeOlder == EventGroupByValue.StateSeeOlder.HAVE_ALERTS -> {
					progressBar.visibility = View.GONE
					seeOlderTextView.visibility = View.VISIBLE
				}
				items.stateSeeOlder == EventGroupByValue.StateSeeOlder.NOT_HAVE_ALERT -> {
					progressBar.visibility = View.GONE
					seeOlderTextView.visibility = View.GONE
				}
			}
		}
	}
}

