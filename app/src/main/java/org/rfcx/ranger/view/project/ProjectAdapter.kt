package org.rfcx.ranger.view.project

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_project.view.*
import org.rfcx.ranger.R
import org.rfcx.ranger.entity.project.Permissions
import org.rfcx.ranger.entity.project.Project
import org.rfcx.ranger.entity.project.isGuest
import org.rfcx.ranger.util.setClickable

class ProjectAdapter(private val listener: ProjectOnClickListener) :
		RecyclerView.Adapter<ProjectAdapter.ProjectSelectViewHolder>() {
	var selectedPosition = -1
	var items: List<Project> = arrayListOf()
		set(value) {
			field = value
			notifyDataSetChanged()
		}
	
	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProjectSelectViewHolder {
		val view =
				LayoutInflater.from(parent.context).inflate(R.layout.item_project, parent, false)
		return ProjectSelectViewHolder(view)
	}
	
	override fun onBindViewHolder(holder: ProjectSelectViewHolder, position: Int) {
		holder.bind(items[position])
		
		if (selectedPosition == position) {
			holder.itemView.checkImageView.visibility = View.VISIBLE
		} else {
			holder.itemView.checkImageView.visibility = View.GONE
		}
		
		holder.itemView.setOnClickListener {
			if (items[position].permissions != Permissions.GUEST.value) {
				selectedPosition = position
				notifyDataSetChanged()
				listener.onClicked(items[position])
			}
		}
	}
	
	override fun getItemCount(): Int = items.size
	
	inner class ProjectSelectViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
		private val locationGroupTextView = itemView.locationGroupTextView
		private val lockImageView = itemView.lockImageView
		
		fun bind(project: Project) {
			locationGroupTextView.text = project.name
			lockImageView.visibility =
					if (project.isGuest()) View.VISIBLE else View.GONE
			setClickable(itemView, project.isGuest())
			
			if (project.isGuest()) {
				locationGroupTextView.setTextColor(ContextCompat.getColor(itemView.context, R.color.text_secondary))
			} else {
				locationGroupTextView.setTextColor(ContextCompat.getColor(itemView.context, R.color.text_primary))
			}
			
			lockImageView.setColorFilter(ContextCompat.getColor(itemView.context,
					R.color.text_secondary))
			
			lockImageView.setOnClickListener {
				listener.onLockImageClicked()
			}
		}
	}
}

interface ProjectOnClickListener {
	fun onClicked(project: Project)
	fun onLockImageClicked()
}
