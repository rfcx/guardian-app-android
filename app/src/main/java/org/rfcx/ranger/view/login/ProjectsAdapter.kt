package org.rfcx.ranger.view.login

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_select_subscribe_projects.view.*
import org.rfcx.ranger.R
import org.rfcx.ranger.entity.OnProjectsItemClickListener
import org.rfcx.ranger.entity.project.Project
import org.rfcx.ranger.entity.project.isGuest

class ProjectsAdapter(val listener: OnProjectsItemClickListener) : RecyclerView.Adapter<ProjectsAdapter.ProjectsViewHolder>() {
	var items: List<ProjectsItem> = arrayListOf()
		set(value) {
			field = value
			notifyDataSetChanged()
		}
	
	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProjectsAdapter.ProjectsViewHolder {
		val view = LayoutInflater.from(parent.context).inflate(R.layout.item_select_subscribe_projects, parent, false)
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
		private val checkBoxImageView = itemView.checkBoxImageView
		private val lockImageView = itemView.lockImageView
		
		fun bind(item: ProjectsItem) {
			textView.text = item.project.name
			lockImageView.visibility = if (item.project.isGuest()) View.VISIBLE else View.GONE
			checkBoxImageView.visibility = if (item.project.isGuest()) View.GONE else View.VISIBLE
			checkBoxImageView.setImageDrawable(ContextCompat.getDrawable(itemView.context, if (item.selected) R.drawable.ic_check_box else R.drawable.ic_check_box_outline))
			lockImageView.setOnClickListener {
				listener.onLockImageClicked()
			}
		}
	}
}

data class ProjectsItem(val project: Project, var selected: Boolean)
