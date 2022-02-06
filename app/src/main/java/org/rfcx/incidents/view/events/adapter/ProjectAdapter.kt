package org.rfcx.incidents.view.events.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import org.rfcx.incidents.R
import org.rfcx.incidents.databinding.ItemProjectBinding
import org.rfcx.incidents.entity.stream.Permissions
import org.rfcx.incidents.entity.stream.Project
import org.rfcx.incidents.entity.stream.isGuest

class ProjectAdapter(private val listener: ProjectOnClickListener) :
    RecyclerView.Adapter<ProjectAdapter.ProjectSelectViewHolder>() {
    var selectedPosition = -1
    var items: List<Project> = arrayListOf()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProjectSelectViewHolder {
        val binding = ItemProjectBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ProjectSelectViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ProjectSelectViewHolder, position: Int) {
        holder.bind(items[position], selectedPosition == position)
        holder.itemView.setOnClickListener {
            if (items[position].permissions != Permissions.GUEST.value) {
                selectedPosition = position
                notifyDataSetChanged()
                listener.onClicked(items[position])
            }
        }
    }

    override fun getItemCount(): Int = items.size

    inner class ProjectSelectViewHolder(binding: ItemProjectBinding) : RecyclerView.ViewHolder(binding.root) {
        private val locationGroupTextView = binding.locationGroupTextView
        private val lockImageView = binding.lockImageView
        private val checkImageView = binding.checkImageView

        fun bind(project: Project, isChecked: Boolean) {
            locationGroupTextView.text = project.name
            lockImageView.visibility =
                if (project.isGuest()) View.VISIBLE else View.GONE
            checkImageView.visibility = if (isChecked) View.VISIBLE else View.GONE
            setClickable(itemView, project.isGuest())

            if (project.isGuest()) {
                locationGroupTextView.setTextColor(ContextCompat.getColor(itemView.context, R.color.text_secondary))
            } else {
                locationGroupTextView.setTextColor(ContextCompat.getColor(itemView.context, R.color.text_primary))
            }

            lockImageView.setColorFilter(
                ContextCompat.getColor(
                    itemView.context,
                    R.color.text_secondary
                )
            )

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

interface ProjectOnClickListener {
    fun onClicked(project: Project)
    fun onLockImageClicked()
}
