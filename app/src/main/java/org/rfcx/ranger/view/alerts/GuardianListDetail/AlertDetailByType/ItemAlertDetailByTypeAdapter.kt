package org.rfcx.ranger.view.alerts.GuardianListDetail.AlertDetailByType

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_alert_detail_by_type.view.*
import org.rfcx.ranger.R
import org.rfcx.ranger.util.EventItem
import org.rfcx.ranger.util.toTimeSinceStringAlternativeTimeAgo
import org.rfcx.ranger.view.alerts.adapter.AlertClickListener

class ItemAlertDetailByTypeAdapter (var items: MutableList<EventItem>, val listener: AlertClickListener) : ListAdapter<EventItem, ItemAlertDetailByTypeAdapter.ItemAlertDetailByTypeViewHolder>(ItemAlertDetailByTypeDiffUtil()) {
	
	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemAlertDetailByTypeViewHolder {
		val view = LayoutInflater.from(parent.context).inflate(R.layout.item_alert_detail_by_type, parent, false)
		return ItemAlertDetailByTypeViewHolder(view)
	}
	
	override fun getItemCount(): Int = items.size
	
	override fun onBindViewHolder(holder: ItemAlertDetailByTypeViewHolder, position: Int) {
		val item = items[position]
		holder.bind(item)
		holder.itemView.setOnClickListener { listener.onClickedAlert(item.event) }
	}
	
	class ItemAlertDetailByTypeDiffUtil : DiffUtil.ItemCallback<EventItem>() {
		override fun areItemsTheSame(oldItem: EventItem, newItem: EventItem): Boolean {
			return oldItem.state == newItem.state
			
		}
		
		override fun areContentsTheSame(oldItem: EventItem, newItem: EventItem): Boolean {
			return oldItem.event.id == newItem.event.id
					&& oldItem.event.value == newItem.event.value
					&& oldItem.state == newItem.state
		}
	}
	
	inner class ItemAlertDetailByTypeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
		private val eventsInEventsTextView = itemView.eventsInEventsTextView
		private val circleImageView = itemView.circleImageView
		private val reviewedImageView = itemView.reviewedImageView
		
		var currentEvent: EventItem? = null
		
		fun bind(item: EventItem) {
			eventsInEventsTextView.text = item.event.beginsAt.toTimeSinceStringAlternativeTimeAgo(itemView.context)
			this.currentEvent = item
			
			when (item.state) {
				EventItem.State.CONFIRM -> {
					reviewedImageView.setImageResource(R.drawable.ic_check)
					reviewedImageView.setBackgroundResource(R.drawable.circle_green_stroke)
					reviewedImageView.visibility = View.VISIBLE
					circleImageView.visibility = View.INVISIBLE
				}
				EventItem.State.REJECT -> {
					reviewedImageView.setImageResource(R.drawable.ic_wrong)
					reviewedImageView.setBackgroundResource(R.drawable.circle_green_stroke)
					reviewedImageView.visibility = View.VISIBLE
					circleImageView.visibility = View.INVISIBLE
				}
				EventItem.State.NONE -> {
					reviewedImageView.visibility = View.INVISIBLE
					circleImageView.visibility = View.VISIBLE
				}
			}
		}
	}
}