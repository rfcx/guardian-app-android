package org.rfcx.ranger.view.login

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_guardian_group.view.*
import org.rfcx.ranger.R
import org.rfcx.ranger.entity.OnProjectsItemClickListener
import org.rfcx.ranger.entity.guardian.GuardianGroup

class ProjectsAdapter(val listener: OnProjectsItemClickListener) : RecyclerView.Adapter<ProjectsAdapter.ProjectsViewHolder>() {
	var items: List<ProjectsItem> = arrayListOf()
		set(value) {
			field = value
			notifyDataSetChanged()
		}
	
	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProjectsAdapter.ProjectsViewHolder {
		val view = LayoutInflater.from(parent.context).inflate(R.layout.item_guardian_group, parent, false)
		return ProjectsViewHolder(view)
	}
	
	override fun getItemCount(): Int = items.size
	
	override fun onBindViewHolder(holder: ProjectsAdapter.ProjectsViewHolder, position: Int) {
		holder.bind(items[position])
		holder.itemView.setOnClickListener {
			listener.onItemClick(items[position], position)
		}
	}
	
	inner class ProjectsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
		private val textView = itemView.guardianGroupTextView
		private val checkImageView = itemView.checkImageView
		
		fun bind(item: ProjectsItem) {
			textView.text = item.projects.name
			checkImageView.visibility = if(item.selected) View.VISIBLE else View.GONE
		}
	}
}

data class ProjectsItem(val projects: GuardianGroup, val selected: Boolean)
