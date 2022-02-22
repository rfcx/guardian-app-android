package org.rfcx.incidents.view.report.submitted

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.rfcx.incidents.R
import org.rfcx.incidents.databinding.ItemSubmittedReportsBinding
import org.rfcx.incidents.entity.response.Response
import org.rfcx.incidents.entity.response.SyncState
import org.rfcx.incidents.entity.response.syncImage
import org.rfcx.incidents.entity.response.syncLabel
import org.rfcx.incidents.util.setDrawableImage
import org.rfcx.incidents.util.toStringWithTimeZone
import org.rfcx.incidents.util.toTimeSinceStringAlternativeTimeAgo
import java.util.TimeZone

class SubmittedReportsAdapter(private val listener: SubmittedReportsOnClickListener) :
    RecyclerView.Adapter<SubmittedReportsAdapter.ReportsViewHolder>() {
    var items: List<ResponseItem> = arrayListOf()
        @SuppressLint("NotifyDataSetChanged")
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubmittedReportsAdapter.ReportsViewHolder {
        val binding = ItemSubmittedReportsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ReportsViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SubmittedReportsAdapter.ReportsViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class ReportsViewHolder(binding: ItemSubmittedReportsBinding) : RecyclerView.ViewHolder(binding.root) {
        private val guardianName = binding.guardianNameTextView
        private val dateTextView = binding.dateTextView
        private val reportIdTextView = binding.reportIdTextView
        private val syncLabelTextView = binding.syncLabelTextView
        private val actionImageView = binding.actionImageView

        fun bind(item: ResponseItem) {
            setClickable(itemView, item.response.syncState != SyncState.SENT.value)
            actionImageView.setDrawableImage(itemView.context, item.response.syncImage())
            reportIdTextView.visibility = if (item.response.incidentRef != null) View.VISIBLE else View.GONE
            reportIdTextView.text = itemView.context.getString(R.string.incident_ref, item.response.incidentRef)
            syncLabelTextView.text = itemView.context.getString(item.response.syncLabel())
            guardianName.text = item.response.streamName
            dateTextView.text = if (item.timeZone == TimeZone.getDefault()) item.response.investigatedAt.toTimeSinceStringAlternativeTimeAgo(
                itemView.context,
                item.timeZone
            ) else item.response.investigatedAt.toStringWithTimeZone(item.timeZone)
            itemView.setOnClickListener {
                listener.onClickedItem(item.response)
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

data class ResponseItem(var response: Response, var timeZone: TimeZone)
