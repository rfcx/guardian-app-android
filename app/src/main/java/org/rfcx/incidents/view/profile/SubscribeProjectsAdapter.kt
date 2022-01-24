package org.rfcx.incidents.view.profile

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_select_subscribe_projects.view.*
import org.rfcx.incidents.R
import org.rfcx.incidents.entity.project.Project

class SubscribeProjectsAdapter : RecyclerView.Adapter<SubscribeProjectsAdapter.GuardianGroupViewHolder>() {
	var items: List<Project> = arrayListOf()
		set(value) {
			field = value
			notifyDataSetChanged()
		}
	
	var mOnItemClickListener: OnItemClickListener? = null
	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GuardianGroupViewHolder {
		val view = LayoutInflater.from(parent.context).inflate(R.layout.item_select_subscribe_projects, parent, false)
		return GuardianGroupViewHolder(view)
	}
	
	override fun getItemCount(): Int = items.size
	
	override fun onBindViewHolder(holder: GuardianGroupViewHolder, position: Int) {
		holder.bind(items[position])
		mOnItemClickListener?.onItemClick(items[position])
	}
	
	inner class GuardianGroupViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
		private val textView = itemView.guardianGroupTextView
		private val checkBoxImageView = itemView.checkBoxImageView
		
		fun bind(project: Project) {
			textView.text = project.name
		}
	}
}
