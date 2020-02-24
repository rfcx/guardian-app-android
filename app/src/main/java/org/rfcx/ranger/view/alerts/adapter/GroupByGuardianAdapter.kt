package org.rfcx.ranger.view.alerts.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_group_by_guardian.view.*
import org.koin.core.KoinComponent
import org.rfcx.ranger.R
import org.rfcx.ranger.view.alerts.EventGroup
import org.rfcx.ranger.view.alerts.OnItemClickListener

class GroupByGuardianAdapter(val listener: OnItemClickListener) : RecyclerView.Adapter<GroupByGuardianAdapter.GroupByGuardianViewHolder>(), KoinComponent {
	var items: List<EventGroup> = arrayListOf()
		set(value) {
			field = value
			notifyDataSetChanged()
		}
	
	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupByGuardianViewHolder {
		val inflater = LayoutInflater.from(parent.context)
		val view = inflater.inflate(R.layout.item_group_by_guardian, parent, false)
		return GroupByGuardianViewHolder(view)
	}
	
	override fun getItemCount(): Int = items.size
	
	override fun onBindViewHolder(holder: GroupByGuardianViewHolder, position: Int) {
		val item = items[position]
		holder.bind(item)
		holder.itemView.setOnClickListener {
			listener.onItemClick(item)
		}
	}
	
	inner class GroupByGuardianViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
		private val groupByGuardianTextView = itemView.groupByGuardianTextView
		private val circleImageView = itemView.circleImageView
		private val numOfEvents = itemView.numOfEventsNotReview
		
		@SuppressLint("SetTextI18n")
		fun bind(eventGroup: EventGroup) {
			groupByGuardianTextView.text = eventGroup.guardianName
			when {
				eventGroup.unReadCount == 0 -> {
					numOfEvents.text = ""
					circleImageView.visibility = View.INVISIBLE
					numOfEvents.visibility = View.INVISIBLE
				}
				eventGroup.unReadCount > 999 -> {
					numOfEvents.text = "999+"
					numOfEvents.visibility = View.VISIBLE
					circleImageView.visibility = View.VISIBLE
				}
				else -> {
					numOfEvents.text = eventGroup.unReadCount.toString()
					numOfEvents.visibility = View.VISIBLE
					circleImageView.visibility = View.VISIBLE
				}
			}
		}
	}
}