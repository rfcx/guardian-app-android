package org.rfcx.ranger.view.alerts.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_alert.view.*
import org.rfcx.ranger.R
import org.rfcx.ranger.entity.event.Event
import org.rfcx.ranger.util.timeAgoDisplay
import org.rfcx.ranger.util.toEventIcon

class AlertsAdapter(val listener: AlertClickListener) : RecyclerView.Adapter<AlertsAdapter.AlertViewHolder>() {
    var items: List<Event> = arrayListOf()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlertViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_alert, parent, false)
        return AlertViewHolder(view)
    }

    override fun onBindViewHolder(holder: AlertViewHolder, position: Int) {
        val item = items[position]
        holder.bind(item)
        holder.itemView.setOnClickListener { listener.onClickedAlert(item) }
    }

    override fun getItemCount(): Int = items.size

    inner class AlertViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvTitle = itemView.tvAlertTitle
        private val iconAlert = itemView.ivAlertIcon
        private val tvFrom = itemView.tvAlertFromSite
        private val tvTimeAgo = itemView.tvAlertTimeAgo
        private val ivStatusRead = itemView.ivStatusRead
        private val ivReviewed = itemView.ivReviewed

        @SuppressLint("SetTextI18n")
        fun bind(event: Event) {
            tvTitle.text = event.value?.trim()?.capitalize()
            event.value?.toEventIcon()?.let { iconAlert.setImageResource(it) }
            tvFrom.text = event.site
            tvTimeAgo.text = "• ${event.timeAgoDisplay(itemView.context)}"
            ivStatusRead.visibility = if (event.reviewerConfirmed != null && event.reviewerConfirmed!!) View.INVISIBLE else View.VISIBLE
            if (event.reviewerConfirmed != null) {
                if (event.reviewerConfirmed!!) {
                    ivReviewed.setImageResource(R.drawable.ic_check)
                    ivReviewed.setBackgroundResource(R.drawable.circle_green_stroke)
                } else {
                    ivReviewed.setImageResource(R.drawable.ic_wrong)
                    ivReviewed.setBackgroundResource(R.drawable.circle_green_stroke)
                }
                ivReviewed.visibility = View.VISIBLE
            } else {
                ivReviewed.visibility = View.INVISIBLE
            }
        }
    }
}