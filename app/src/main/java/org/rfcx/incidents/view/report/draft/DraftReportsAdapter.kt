package org.rfcx.incidents.view.report.draft

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import org.rfcx.incidents.R
import org.rfcx.incidents.databinding.ItemDraftReportsBinding
import org.rfcx.incidents.entity.response.Response
import org.rfcx.incidents.entity.response.SyncState
import org.rfcx.incidents.util.setDrawableImage
import org.rfcx.incidents.util.toTimeSinceStringAlternativeTimeAgo

class DraftReportsAdapter(private val listener: ReportOnClickListener) :
    RecyclerView.Adapter<DraftReportsAdapter.ReportsViewHolder>() {
    var items: List<Response> = arrayListOf()
        @SuppressLint("NotifyDataSetChanged")
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DraftReportsAdapter.ReportsViewHolder {
        val binding = ItemDraftReportsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ReportsViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DraftReportsAdapter.ReportsViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class ReportsViewHolder(binding: ItemDraftReportsBinding) : RecyclerView.ViewHolder(binding.root) {

        private val guardianName = binding.guardianNameTextView
        private val dateTextView = binding.dateTextView
        private val actionImageView = binding.actionImageView

        fun bind(report: Response) {
            actionImageView.setDrawableImage(itemView.context, R.drawable.ic_delete_outline)
            setClickable(itemView, report.syncState == SyncState.SENT.value)

            if (report.syncState == SyncState.SENT.value) {
                guardianName.setTextColor(ContextCompat.getColor(itemView.context, R.color.text_secondary))
                dateTextView.setTextColor(ContextCompat.getColor(itemView.context, R.color.text_secondary))
            } else {
                guardianName.setTextColor(ContextCompat.getColor(itemView.context, R.color.text_black))
                dateTextView.setTextColor(ContextCompat.getColor(itemView.context, R.color.text_black))
            }

            guardianName.text = report.streamName
            dateTextView.text = report.investigatedAt.toTimeSinceStringAlternativeTimeAgo(itemView.context)

            actionImageView.setOnClickListener {
                listener.onClickedDelete(report)
            }

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

interface ReportOnClickListener {
    fun onClickedDelete(response: Response)
    fun onClickedItem(response: Response)
}
