package org.rfcx.ranger.view.alerts.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_group_by_guardian.view.*
import org.rfcx.ranger.R
import org.rfcx.ranger.entity.guardian.Guardian
import org.rfcx.ranger.view.alerts.OnItemClickListener

class GroupByGuardianAdapter : RecyclerView.Adapter<GroupByGuardianAdapter.GroupByGuardianViewHolder>() {
	
	var items: List<Guardian> = arrayListOf()
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
		holder.bind(items[position])
	}
	
	inner class GroupByGuardianViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
		private val groupByGuardianTextView = itemView.groupByGuardianTextView
		private val circleImageView = itemView.circleImageView
		
		var currentGroup: Guardian? = null
		
		init {
			itemView.setOnClickListener {
				currentGroup?.let { it1 -> mOnItemClickListener?.onItemClick(it1) }
			}
		}
		
		fun bind(group: Guardian) {
			circleImageView.visibility = View.VISIBLE
			groupByGuardianTextView.text = group.name
			
			this.currentGroup = group
		}
	}
}