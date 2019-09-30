package org.rfcx.ranger.view.alerts.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_group_by_guardian.view.*
import org.rfcx.ranger.R
import org.rfcx.ranger.entity.event.Event
import org.rfcx.ranger.view.alerts.GroupGuardianAlert
import org.rfcx.ranger.view.alerts.OnItemClickListener

class GroupByGuardianAdapter : RecyclerView.Adapter<GroupByGuardianAdapter.GroupByGuardianViewHolder>() {

    var items: List<GroupGuardianAlert> = arrayListOf()
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
        holder.bind(items[position].copy())
    }

    inner class GroupByGuardianViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val groupByGuardianTextView = itemView.groupByGuardianTextView
        private val circleImageView = itemView.circleImageView
        private val numOfEvents = itemView.numOfEventsNotReview

        var currentGroup: ArrayList<Event>? = null
        var name: String = ""

        init {
            itemView.setOnClickListener {
                mOnItemClickListener?.onItemClick(currentGroup, name)
            }
        }

        fun bind(groupGuardianAlert: GroupGuardianAlert) {
            groupByGuardianTextView.text = groupGuardianAlert.name
            if (groupGuardianAlert.unread == null) {
                circleImageView.visibility = View.INVISIBLE
                numOfEvents.visibility = View.INVISIBLE
            } else {
                numOfEvents.text = groupGuardianAlert.unread.toString()
                numOfEvents.visibility = View.VISIBLE
                circleImageView.visibility = View.VISIBLE
            }
            this.currentGroup = groupGuardianAlert.events
            this.name = groupGuardianAlert.name
        }
    }
}