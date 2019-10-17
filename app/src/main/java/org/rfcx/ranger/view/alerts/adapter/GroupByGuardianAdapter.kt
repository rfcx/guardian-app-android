package org.rfcx.ranger.view.alerts.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_group_by_guardian.view.*
import org.koin.core.KoinComponent
import org.koin.core.inject
import org.rfcx.ranger.R
import org.rfcx.ranger.data.local.EventDb
import org.rfcx.ranger.entity.event.Event
import org.rfcx.ranger.view.alerts.EventGroup
import org.rfcx.ranger.view.alerts.OnItemClickListener

class GroupByGuardianAdapter : RecyclerView.Adapter<GroupByGuardianAdapter.GroupByGuardianViewHolder>(), KoinComponent {
	
	val eventsDb: EventDb by inject()
	
	var items: List<EventGroup> = arrayListOf()
		set(value) {
			field = value
			notifyDataSetChanged()
		}
	
	var mOnItemClickListener: OnItemClickListener? = null
	
	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupByGuardianViewHolder {
		val view = LayoutInflater.from(parent.context).inflate(R.layout.item_group_by_guardian, parent, false)
		return GroupByGuardianViewHolder(view)
	}
	
	override fun getItemCount(): Int = items.size
	
	override fun onBindViewHolder(holder: GroupByGuardianViewHolder, position: Int) {
		holder.bind(items[position].copy())
	}
	
	inner class GroupByGuardianViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
		private val groupByGuardianTextView = itemView.groupByGuardianTextView
		private val circleImageView = itemView.circleImageView
		private val numOfEvents = itemView.numOfEventsNotReview
		
		var currentGroup: List<Event> = listOf()
		var name: String = ""
		
		init {
			itemView.setOnClickListener {
				mOnItemClickListener?.onItemClick(currentGroup, name)
			}
		}
		
		fun bind(eventGroup: EventGroup) {
			val unread = eventGroup.numberOfUnread(eventsDb)
			groupByGuardianTextView.text = eventGroup.guardianName
			if (unread == 0) {
				circleImageView.visibility = View.INVISIBLE
				numOfEvents.visibility = View.INVISIBLE
			} else {
				numOfEvents.text = unread.toString()
				numOfEvents.visibility = View.VISIBLE
				circleImageView.visibility = View.VISIBLE
			}
			this.currentGroup = eventGroup.events
			this.name = eventGroup.guardianName
		}
	}
}