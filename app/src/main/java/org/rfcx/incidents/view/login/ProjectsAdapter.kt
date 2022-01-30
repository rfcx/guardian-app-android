package org.rfcx.incidents.view.login

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import org.rfcx.incidents.R
import org.rfcx.incidents.databinding.ItemSelectSubscribeProjectsBinding
import org.rfcx.incidents.entity.OnProjectsItemClickListener
import org.rfcx.incidents.entity.project.Project
import org.rfcx.incidents.entity.project.isGuest

class ProjectsAdapter(val listener: OnProjectsItemClickListener) :
    RecyclerView.Adapter<ProjectsAdapter.ProjectsViewHolder>() {
    var items: List<ProjectsItem> = arrayListOf()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    var subscribingProject: String? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProjectsAdapter.ProjectsViewHolder {
        val binding = ItemSelectSubscribeProjectsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ProjectsViewHolder(binding)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: ProjectsAdapter.ProjectsViewHolder, position: Int) {
        holder.bind(items[position])
        holder.itemView.setOnClickListener {
            listener.onItemClick(items[position], position)
        }
    }

    inner class ProjectsViewHolder(binding: ItemSelectSubscribeProjectsBinding) : RecyclerView.ViewHolder(binding.root) {
        private val textView = binding.guardianGroupTextView
        private val checkBoxImageView = binding.checkBoxImageView
        private val lockImageView = binding.lockImageView
        private val subscribeProgress = binding.subscribeProgress

        fun bind(item: ProjectsItem) {
            subscribeProgress.visibility = if (subscribingProject == item.project.name) View.VISIBLE else View.GONE
            setClickable(itemView, item.project.isGuest() || items.any { p -> subscribingProject == p.project.name })
            if (item.project.isGuest()) {
                textView.setTextColor(ContextCompat.getColor(itemView.context, R.color.text_secondary))
            } else {
                textView.setTextColor(ContextCompat.getColor(itemView.context, R.color.text_primary))
            }

            if (items.any { p -> subscribingProject == p.project.name }) {
                checkBoxImageView.setColorFilter(ContextCompat.getColor(itemView.context, R.color.text_secondary))
            } else {
                checkBoxImageView.setColorFilter(ContextCompat.getColor(itemView.context, R.color.text_primary))
            }

            textView.text = item.project.name
            lockImageView.visibility = if (item.project.isGuest()) View.VISIBLE else View.GONE
            checkBoxImageView.visibility = if (item.project.isGuest()) View.GONE else View.VISIBLE
            checkBoxImageView.setImageDrawable(
                ContextCompat.getDrawable(
                    itemView.context,
                    if (item.selected) R.drawable.ic_check_box else R.drawable.ic_check_box_outline
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

data class ProjectsItem(val project: Project, var selected: Boolean)
