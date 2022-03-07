package org.rfcx.incidents.view.report.draft

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.rfcx.incidents.R
import org.rfcx.incidents.databinding.ItemDraftReportsBinding
import org.rfcx.incidents.entity.response.InvestigationType
import org.rfcx.incidents.entity.response.Response
import org.rfcx.incidents.entity.response.SyncState
import org.rfcx.incidents.entity.response.syncImage
import org.rfcx.incidents.util.setDrawableImage
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
        private val loggingTextView = binding.loggingTextView
        private val notHaveImageView = binding.notHaveImageView
        private val poachingTextView = binding.poachingTextView
        private val otherTextView = binding.otherTextView
        private val reportIdTextView = binding.reportIdTextView

        fun bind(item: Triple<Response, TimeZone?, String?>) {
            val (response, timeZone, image) = item
            val isSend = response.syncState == SyncState.SENT.value

            image?.let { imageView.setImage(it) }
            notHaveImageView.visibility = if (image == null) View.VISIBLE else View.GONE

            loggingTextView.visibility = if (response.investigateType.contains(InvestigationType.LOGGING.value)) View.VISIBLE else View.GONE
            poachingTextView.visibility = if (response.investigateType.contains(InvestigationType.POACHING.value)) View.VISIBLE else View.GONE
            otherTextView.visibility = if (response.investigateType.contains(InvestigationType.OTHER.value)) View.VISIBLE else View.GONE

            actionImageView.visibility = if (isSend) View.VISIBLE else View.GONE
            actionImageView.setDrawableImage(itemView.context, response.syncImage())

            reportIdTextView.visibility = if (response.incidentRef != null) View.VISIBLE else View.GONE
            reportIdTextView.text = itemView.context.getString(R.string.incident_ref, response.incidentRef)

            guardianName.text = response.streamName
            dateTextView.text = if (timeZone == TimeZone.getDefault()) response.investigatedAt.toTimeSinceStringAlternativeTimeAgo(
                itemView.context,
                timeZone
            ) else response.investigatedAt.toStringWithTimeZone(timeZone)

            itemView.setOnClickListener {
                listener.onClickedItem(response)
            }
        }
    }
}

interface ReportOnClickListener {
    fun onClickedItem(response: Response)
}
