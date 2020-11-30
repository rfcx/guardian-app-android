package org.rfcx.ranger.view.login

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_guardian_group.view.*
import org.rfcx.ranger.R
import org.rfcx.ranger.entity.guardian.GuardianGroup
import org.rfcx.ranger.view.profile.OnItemClickListener

class ProjectsAdapter : RecyclerView.Adapter<ProjectsAdapter.ProjectsViewHolder>() {
	var items: List<GuardianGroup> = arrayListOf()
		set(value) {
			field = value
			notifyDataSetChanged()
		}
	
	var onItemClickListener: OnItemClickListener? = null
	
	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProjectsAdapter.ProjectsViewHolder {
		val view = LayoutInflater.from(parent.context).inflate(R.layout.item_guardian_group, parent, false)
		return ProjectsViewHolder(view)
	}
	
	override fun getItemCount(): Int = items.size
	
	override fun onBindViewHolder(holder: ProjectsAdapter.ProjectsViewHolder, position: Int) {
		holder.bind(items[position])
	}
	
	inner class ProjectsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
		private val textView = itemView.guardianGroupTextView
		private var currentGroup: GuardianGroup? = null
		
		init {
			itemView.setOnClickListener {
				currentGroup?.let { it1 -> onItemClickListener?.onItemClick(it1) }
			}
		}
		
		fun bind(group: GuardianGroup?) {
			textView.text = group?.name
			this.currentGroup = group
		}
	}
}
