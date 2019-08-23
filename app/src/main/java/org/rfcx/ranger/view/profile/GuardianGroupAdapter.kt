package org.rfcx.ranger.view.profile

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_guardian_group.view.*
import org.rfcx.ranger.R
import org.rfcx.ranger.entity.guardian.GuardianGroup

class GuardianGroupAdapter : RecyclerView.Adapter<GuardianGroupAdapter.GuardianGroupViewHolder>() {
	var items: List<GuardianGroup> = arrayListOf()
		set(value) {
			field = value
			notifyDataSetChanged()
		}
	
	var mOnItemClickListener: OnItemClickListener? = null
	
	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GuardianGroupViewHolder {
		val view = LayoutInflater.from(parent.context).inflate(R.layout.item_guardian_group, parent, false)
		return GuardianGroupViewHolder(view)
	}
	
	override fun getItemCount(): Int = items.size
	
	override fun onBindViewHolder(holder: GuardianGroupViewHolder, position: Int) {
		holder.bind(items[position])
	}
	
	inner class GuardianGroupViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
		private val textView = itemView.guardianGroupTextView
		
		var currentGroup: GuardianGroup? = null
		
		init {
			itemView.setOnClickListener {
				currentGroup?.let { it1 -> mOnItemClickListener?.onItemClick(it1) }
			}
		}
		
		fun bind(group: GuardianGroup?) {
			textView.text = group?.name
			
			this.currentGroup = group
		}
	}
}