package org.rfcx.ranger.view.alerts.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_guardian_list_detail.view.*
import org.rfcx.ranger.R
import org.rfcx.ranger.entity.event.Event
import org.rfcx.ranger.view.alerts.OnItemClickEventValuesListener

class GuardianListDetailAdapter : RecyclerView.Adapter<GuardianListDetailAdapter.GuardianListDetailViewHolder>() {
	
	var items: List<Event> = arrayListOf()
		set(value) {
			field = value
			notifyDataSetChanged()
		}
	
	var mOnItemClickListener: OnItemClickEventValuesListener? = null
	
	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GuardianListDetailViewHolder {
		val view = LayoutInflater.from(parent.context).inflate(R.layout.item_guardian_list_detail, parent, false)
		return GuardianListDetailViewHolder(view)
	}
	
	override fun getItemCount(): Int = items.size
	
	override fun onBindViewHolder(holder: GuardianListDetailViewHolder, position: Int) {
		holder.bind(items[position])
	}
	
	inner class GuardianListDetailViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
		private val groupByGuardianTextView = itemView.eventsInGuardianTextView
		private val circleImageView = itemView.circleImageView
		
		var currentGroup: Event? = null
		
		init {
			itemView.setOnClickListener {
				currentGroup?.let { it1 -> mOnItemClickListener?.onItemClick(it1) }
			}
		}
		
		fun bind(event: Event) {
			circleImageView.visibility = View.VISIBLE
			Log.d("Event", event.value)
			groupByGuardianTextView.text = event.value
			
			this.currentGroup = event
		}
	}
}