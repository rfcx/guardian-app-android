package org.rfcx.incidents.view.login

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_select_subscribe_projects.view.*
import org.rfcx.incidents.R
import org.rfcx.incidents.entity.OnProjectsItemClickListener
import org.rfcx.incidents.entity.project.Project
import org.rfcx.incidents.entity.project.isGuest

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
		private val subscribeProgress = itemView.subscribeProgress
		
		fun bind(item: ProjectsItem) {
			subscribeProgress.visibility = if (item.subscribeProgress) View.VISIBLE else View.GONE
			setClickable(itemView, item.project.isGuest() || items.any { p -> p.subscribeProgress })
			if (item.project.isGuest()) {
				textView.setTextColor(ContextCompat.getColor(itemView.context, R.color.text_secondary))
			} else {
				textView.setTextColor(ContextCompat.getColor(itemView.context, R.color.text_primary))
			}
			textView.text = item.project.name
			lockImageView.visibility = if (item.project.isGuest()) View.VISIBLE else View.GONE
			checkBoxImageView.visibility = if (item.project.isGuest()) View.GONE else View.VISIBLE
			checkBoxImageView.setImageDrawable(ContextCompat.getDrawable(itemView.context, if (item.selected) R.drawable.ic_check_box else R.drawable.ic_check_box_outline))
			lockImageView.setOnClickListener {
				listener.onLockImageClicked()
			}
		}
		
		private fun setClickable(view: View?, clickable: Boolean) {
			if (view == null) return
			
			if (view is ViewGroup) {
				for (i in 0 until view.childCount) {
					setClickable(view.getChildAt(i), clickable)
				}
			}
			view.isClickable = clickable
		}
	}
}

data class ProjectsItem(val project: Project, var selected: Boolean, var subscribeProgress: Boolean = false)
