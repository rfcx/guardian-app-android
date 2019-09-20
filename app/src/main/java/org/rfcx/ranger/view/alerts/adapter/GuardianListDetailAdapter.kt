package org.rfcx.ranger.view.alerts.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_guardian_list_detail.view.*
import org.rfcx.ranger.R
import org.rfcx.ranger.entity.event.Event
import org.rfcx.ranger.view.alerts.OnItemClickEventValuesListener

class GuardianListDetailAdapter : RecyclerView.Adapter<GuardianListDetailAdapter.GuardianListDetailViewHolder>() {
	
	private val viewPool = RecyclerView.RecycledViewPool()
	var stutasVisibility: ArrayList<Boolean> = arrayListOf()
	var currentEventList: MutableList<Event>? = null
	
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
		holder.bind(allItem[position], position)
	}
	
	inner class GuardianListDetailViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
		private val groupByGuardianTextView = itemView.guardianListDetailTextView
		private val circleImageView = itemView.circleImageView
		private val numOfEventsNotOpen = itemView.numOfEventsNotOpen
		private val guardianListDetailRecycler = itemView.guardianListDetailRecycler
		
		fun bind(eventList: MutableList<Event>, position: Int) {
			circleImageView.visibility = View.VISIBLE
			groupByGuardianTextView.text = eventList[0].value
			numOfEventsNotOpen.text = eventList.size.toString()
			stutasVisibility.add(position, false)
			
			guardianListDetailRecycler.apply {
				layoutManager = LinearLayoutManager(context)
				adapter = EventsInEventNameAdater(eventList)
				setRecycledViewPool(viewPool)
			}
			
			itemView.setOnClickListener {
				currentEventList?.let { it1 -> mOnItemClickListener?.onItemClick(it1) }
				if (stutasVisibility[position]) {
					guardianListDetailRecycler.visibility = View.GONE
					stutasVisibility.add(position, false)
				} else {
					guardianListDetailRecycler.visibility = View.VISIBLE
					stutasVisibility.add(position, true)
				}
			}
			currentEventList = eventList
		}
	}
}