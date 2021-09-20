package org.rfcx.ranger.view.events.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_guardian.view.*
import org.rfcx.ranger.R
import org.rfcx.ranger.entity.event.Event
import org.rfcx.ranger.util.setFormatLabel

class GuardianItemAdapter(private val onClickListener: (EventGroup) -> Unit) : RecyclerView.Adapter<GuardianItemAdapter.GuardianItemViewHolder>() {
	var items: List<EventGroup> = arrayListOf()
		@SuppressLint("NotifyDataSetChanged")
		set(value) {
			field = value
			notifyDataSetChanged()
		}
	
	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GuardianItemViewHolder {
		val view = LayoutInflater.from(parent.context).inflate(R.layout.item_guardian, parent, false)
		return GuardianItemViewHolder(view)
	}
	
	override fun getItemCount(): Int = items.size
	
	override fun onBindViewHolder(holder: GuardianItemViewHolder, position: Int) {
		holder.bind(items[position])
		holder.itemView.setOnClickListener {
			onClickListener(items[position])
		}
	}
	
	inner class GuardianItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
		private val numberImageView = itemView.numberOfAlertsImageView
		private val numberOfAlerts = itemView.numberOfAlertsTextView
		private val guardianName = itemView.guardianNameTextView
		private val distance = itemView.distanceTextView
		
		fun bind(item: EventGroup) {
			guardianName.text = item.guardianName
			distance.text = item.distance.setFormatLabel()
			numberOfAlerts.text = item.events.size.toString()

			if (item.events.isEmpty()) {
				numberImageView.setImageResource(R.drawable.bg_circle_green)
			} else {
				numberImageView.setImageResource(R.drawable.bg_circle_red)
			}
		}
	}
}

data class GuardianModel(var name: String, var numberOfAlerts: Int, val distance: Float) //TODO:: Change to real model

data class EventGroup(val events: List<Event>, val distance: Double, val guardianName: String)
