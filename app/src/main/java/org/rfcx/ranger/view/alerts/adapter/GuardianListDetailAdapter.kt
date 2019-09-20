package org.rfcx.ranger.view.alerts.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_guardian_list_detail.view.*
import org.rfcx.ranger.R
import org.rfcx.ranger.entity.event.Event
import org.rfcx.ranger.view.alerts.OnItemClickEventValuesListener

class GuardianListDetailAdapter : RecyclerView.Adapter<GuardianListDetailAdapter.GuardianListDetailViewHolder>() {
	
	
	var allItem: ArrayList<MutableList<Event>> = arrayListOf()
		set(value) {
			field = value
			notifyDataSetChanged()
		}
	
	var mOnItemClickListener: OnItemClickEventValuesListener? = null
	
	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GuardianListDetailViewHolder {
		val view = LayoutInflater.from(parent.context).inflate(R.layout.item_guardian_list_detail, parent, false)
		return GuardianListDetailViewHolder(view)
	}
	
	override fun getItemCount(): Int = allItem.size
	
	override fun onBindViewHolder(holder: GuardianListDetailViewHolder, position: Int) {
		holder.bind(allItem[position])
	}
	
	inner class GuardianListDetailViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
		private val groupByGuardianTextView = itemView.guardianListDetailTextView
		private val circleImageView = itemView.circleImageView
		private val numOfEventsNotOpen = itemView.numOfEventsNotOpen
		
		var currentEventList: MutableList<Event>? = null
		
		init {
			itemView.setOnClickListener {
				currentEventList?.let { it1 -> mOnItemClickListener?.onItemClick(it1) }
			}
		}
		
		fun bind(eventList: MutableList<Event>) {
			circleImageView.visibility = View.VISIBLE
			groupByGuardianTextView.text = eventList[0].value
			numOfEventsNotOpen.text = eventList.size.toString()
			
			// list of event
			
			this.currentEventList = eventList
		}
	}
}