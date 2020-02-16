package org.rfcx.ranger.view.alerts.guardian

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_guardian_list_detail.view.*
import org.rfcx.ranger.R
import org.rfcx.ranger.util.toEventIcon

class GuardianDetailAdapter : ListAdapter<EventGroupItem,
		GuardianDetailAdapter.GuardianViewHolder>(GuardianItemDiffUtil()){
	
	var mOnItemViewClickListener: OnItemViewClickListener? = null
	
	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GuardianViewHolder {
		val view = LayoutInflater.from(parent.context).inflate(R.layout.item_guardian_list_detail, parent, false)
		return GuardianViewHolder(view)
	}
	
	override fun onBindViewHolder(holder: GuardianViewHolder, position: Int) {
		holder.bind(getItem(position))
	}
	
	class GuardianItemDiffUtil : DiffUtil.ItemCallback<EventGroupItem>() {
		override fun areItemsTheSame(oldItem: EventGroupItem, newItem: EventGroupItem): Boolean {
			return oldItem.value == newItem.value
			
		}
		
		override fun areContentsTheSame(oldItem: EventGroupItem, newItem: EventGroupItem): Boolean {
			return oldItem.unReviewedCount == newItem.unReviewedCount
					&& oldItem.events.size == newItem.events.size
					&& oldItem.events == newItem.events
		}
	}
	
	inner class GuardianViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
		private val typeNameTextView = itemView.eventTypeTextView
		private val countTextView = itemView.countTextView
		private val circleImageView = itemView.circleImageView
		private val iconAlert = itemView.ivAlertIcon
		
		@SuppressLint("DefaultLocale")
		fun bind(eventGroupItem: EventGroupItem) {
			eventGroupItem.value.toEventIcon().let { iconAlert.setImageResource(it) }
			if (eventGroupItem.unReviewedCount == 0) {
				countTextView.visibility = View.INVISIBLE
				circleImageView.visibility = View.INVISIBLE
			} else {
				countTextView.text = if (eventGroupItem.unReviewedCount > 999) {
					"999+"
				} else {
					eventGroupItem.unReviewedCount.toString()
				}
				countTextView.visibility = View.VISIBLE
				circleImageView.visibility = View.VISIBLE
			}
			typeNameTextView.text = eventGroupItem.events[0].label.capitalize()
			itemView.setOnClickListener {
				mOnItemViewClickListener?.onItemViewClick(eventGroupItem.events[0].value,
						eventGroupItem.events[0].label, eventGroupItem.events[0].guardianName)
			}
		}
	}
}