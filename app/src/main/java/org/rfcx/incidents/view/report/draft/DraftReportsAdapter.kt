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
import org.rfcx.incidents.util.toStringWithTimeZone
import org.rfcx.incidents.util.toTimeSinceStringAlternativeTimeAgo
import org.rfcx.incidents.view.report.submitted.ResponseItem
import java.util.TimeZone

class DraftReportsAdapter(private val listener: ReportOnClickListener) :
    RecyclerView.Adapter<DraftReportsAdapter.ReportsViewHolder>() {
    var items: List<ResponseItem> = arrayListOf()
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

        fun bind(item: ResponseItem) {
            actionImageView.setDrawableImage(itemView.context, R.drawable.ic_delete_outline)
            setClickable(itemView, item.response.syncState == SyncState.SENT.value)

            if (item.response.syncState == SyncState.SENT.value) {
                guardianName.setTextColor(ContextCompat.getColor(itemView.context, R.color.text_secondary))
                dateTextView.setTextColor(ContextCompat.getColor(itemView.context, R.color.text_secondary))
            } else {
                guardianName.setTextColor(ContextCompat.getColor(itemView.context, R.color.text_black))
                dateTextView.setTextColor(ContextCompat.getColor(itemView.context, R.color.text_black))
            }

            guardianName.text = item.response.streamName
            dateTextView.text = if (item.timeZone == TimeZone.getDefault()) item.response.investigatedAt.toTimeSinceStringAlternativeTimeAgo(
                itemView.context,
                item.timeZone
            ) else item.response.investigatedAt.toStringWithTimeZone(item.timeZone)

            actionImageView.setOnClickListener {
                listener.onClickedDelete(item.response)
            }

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

interface ReportOnClickListener {
    fun onClickedDelete(response: Response)
    fun onClickedItem(response: Response)
}
