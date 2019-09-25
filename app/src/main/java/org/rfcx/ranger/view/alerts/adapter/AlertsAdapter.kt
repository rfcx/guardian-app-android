package org.rfcx.ranger.view.alerts.adapter

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_alert.view.*
import org.rfcx.ranger.R
import org.rfcx.ranger.entity.event.Event
import org.rfcx.ranger.util.timeAgoDisplay
import org.rfcx.ranger.util.toEventIcon


class AlertsAdapter(val listener: AlertClickListener) : ListAdapter<EventItem, AlertsAdapter.AlertViewHolder>(AlertsDiffUtil()) {
	
	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlertViewHolder {
		val view = LayoutInflater.from(parent.context).inflate(R.layout.item_alert, parent, false)
		return AlertViewHolder(view)
	}
	
	override fun onBindViewHolder(holder: AlertViewHolder, position: Int) {
		val item = getItem(position)
		holder.bind(item)
		holder.itemView.setOnClickListener { listener.onClickedAlert(item.event) }
	}
	
	class AlertsDiffUtil : DiffUtil.ItemCallback<EventItem>() {
		override fun areItemsTheSame(oldItem: EventItem, newItem: EventItem): Boolean {
			return oldItem.state == newItem.state
			
		}
		
		override fun areContentsTheSame(oldItem: EventItem, newItem: EventItem): Boolean {
			return oldItem.event.event_guid == newItem.event.event_guid
					&& oldItem.event.value == newItem.event.value
					&& oldItem.state == newItem.state
		}
	}
	
	inner class AlertViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
		private val tvTitle = itemView.tvAlertTitle
		private val iconAlert = itemView.ivAlertIcon
		private val tvFrom = itemView.tvAlertFromSite
		private val tvTimeAgo = itemView.tvAlertTimeAgo
		private val ivStatusRead = itemView.ivStatusRead
		private val ivReviewed = itemView.ivReviewed
		
		@SuppressLint("SetTextI18n", "DefaultLocale")
		fun bind(item: EventItem) {
			tvTitle.text = item.event.guardianShortname
			item.event.value?.toEventIcon()?.let { iconAlert.setImageResource(it) }
			if (item.event.site != null) {
				tvFrom.text = item.event.site!!.capitalize()
			}
			tvTimeAgo.text = "• ${item.event.timeAgoDisplay(itemView.context)}"
			when (item.state) {
				EventItem.State.CONFIRM -> {
					ivReviewed.setImageResource(R.drawable.ic_check)
					ivReviewed.setBackgroundResource(R.drawable.circle_green_stroke)
					ivReviewed.visibility = View.VISIBLE
					ivStatusRead.visibility = View.INVISIBLE
				}
				EventItem.State.REJECT -> {
					ivReviewed.setImageResource(R.drawable.ic_wrong)
					ivReviewed.setBackgroundResource(R.drawable.circle_green_stroke)
					ivReviewed.visibility = View.VISIBLE
					ivStatusRead.visibility = View.INVISIBLE
				}
				EventItem.State.NONE -> {
					ivReviewed.visibility = View.INVISIBLE
					ivStatusRead.visibility = View.VISIBLE
				}
			}
		}
	}
}

data class EventItem(val event: Event, var state: State = State.NONE) {
	
	enum class State {
		CONFIRM, REJECT, NONE
	}
}