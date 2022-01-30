package org.rfcx.incidents.view.report.submitted

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_submitted_reports.view.*
import org.rfcx.incidents.R
import org.rfcx.incidents.entity.response.Response
import org.rfcx.incidents.entity.response.SyncState
import org.rfcx.incidents.entity.response.syncImage
import org.rfcx.incidents.entity.response.syncLabel
import org.rfcx.incidents.util.setDrawableImage
import org.rfcx.incidents.util.toTimeSinceStringAlternativeTimeAgo

class SubmittedReportsAdapter(private val listener: SubmittedReportsOnClickListener) :
    RecyclerView.Adapter<SubmittedReportsAdapter.ReportsViewHolder>() {
    var items: List<Response> = arrayListOf()
        @SuppressLint("NotifyDataSetChanged")
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubmittedReportsAdapter.ReportsViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_submitted_reports, parent, false)
        return ReportsViewHolder(view)
    }

    override fun onBindViewHolder(holder: SubmittedReportsAdapter.ReportsViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class ReportsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val guardianName = itemView.guardianNameTextView
        private val dateTextView = itemView.dateTextView
        private val reportIdTextView = itemView.reportIdTextView
        private val syncLabelTextView = itemView.syncLabelTextView
        private val actionImageView = itemView.actionImageView

        fun bind(report: Response) {
            setClickable(itemView, report.syncState != SyncState.SENT.value)
            actionImageView.setDrawableImage(itemView.context, report.syncImage())
            reportIdTextView.visibility = if (report.incidentRef != null) View.VISIBLE else View.GONE
            reportIdTextView.text = itemView.context.getString(R.string.incident_ref, report.incidentRef)
            syncLabelTextView.text = itemView.context.getString(report.syncLabel())
            guardianName.text = report.streamName
            dateTextView.text = report.investigatedAt.toTimeSinceStringAlternativeTimeAgo(itemView.context)

            itemView.setOnClickListener {
                listener.onClickedItem(report)
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

interface SubmittedReportsOnClickListener {
    fun onClickedItem(response: Response)
}
