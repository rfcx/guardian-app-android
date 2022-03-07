package org.rfcx.incidents.view.report.draft

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import org.rfcx.incidents.R
import org.rfcx.incidents.databinding.ItemDraftReportsBinding
import org.rfcx.incidents.entity.response.InvestigationType
import org.rfcx.incidents.entity.response.Response
import org.rfcx.incidents.entity.response.SyncState
import org.rfcx.incidents.util.setImage
import org.rfcx.incidents.util.toStringWithTimeZone
import org.rfcx.incidents.util.toTimeSinceStringAlternativeTimeAgo
import java.util.TimeZone

class DraftReportsAdapter(private val listener: ReportOnClickListener) :
    RecyclerView.Adapter<DraftReportsAdapter.ReportsViewHolder>() {
    var items: List<Triple<Response, TimeZone?, String?>> = arrayListOf()
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
        private val imageView = binding.imageView
        private val imageCardView = binding.imageCardView
        private val progressBarOfImageView = binding.progressBarOfImageView
        private val loggingTextView = binding.loggingTextView
        private val poachingTextView = binding.poachingTextView
        private val otherTextView = binding.otherTextView

        fun bind(item: Triple<Response, TimeZone?, String?>) {
            val (response, timeZone, image) = item
            setClickable(itemView, response.syncState == SyncState.SENT.value)

            image?.let { imageView.setImage(it) }
            imageCardView.visibility = if (image == null) View.GONE else View.VISIBLE

            loggingTextView.visibility = if (response.investigateType.contains(InvestigationType.LOGGING.value)) View.VISIBLE else View.GONE
            poachingTextView.visibility = if (response.investigateType.contains(InvestigationType.POACHING.value)) View.VISIBLE else View.GONE
            otherTextView.visibility = if (response.investigateType.contains(InvestigationType.OTHER.value)) View.VISIBLE else View.GONE

            guardianName.text = response.streamName
            dateTextView.text = if (timeZone == TimeZone.getDefault()) response.investigatedAt.toTimeSinceStringAlternativeTimeAgo(
                itemView.context,
                timeZone
            ) else response.investigatedAt.toStringWithTimeZone(timeZone)

            actionImageView.setOnClickListener {
                listener.onClickedDelete(response)
            }

            itemView.setOnClickListener {
                listener.onClickedItem(response)
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
