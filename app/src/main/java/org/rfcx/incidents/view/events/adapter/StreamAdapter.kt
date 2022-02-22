package org.rfcx.incidents.view.events.adapter

import android.content.res.Resources
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.rfcx.incidents.R
import org.rfcx.incidents.databinding.ItemStreamBinding
import org.rfcx.incidents.entity.event.Event
import org.rfcx.incidents.entity.stream.GuardianType
import org.rfcx.incidents.entity.stream.Stream
import org.rfcx.incidents.util.dateRangeFormat
import org.rfcx.incidents.util.setShortTimeZone
import java.util.TimeZone

class StreamAdapter(private val onClickListener: (Stream) -> Unit) :
    RecyclerView.Adapter<StreamAdapter.StreamViewHolder>() {
    var items: List<Stream> = arrayListOf()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StreamViewHolder {
        val binding = ItemStreamBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return StreamViewHolder(binding)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: StreamViewHolder, position: Int) {
        holder.bind(items[position])
        holder.itemView.setOnClickListener {
            onClickListener(items[position])
        }
    }

    inner class StreamViewHolder(binding: ItemStreamBinding) : RecyclerView.ViewHolder(binding.root) {
        private val guardianName = binding.guardianNameTextView
        private val timeTextView = binding.timeTextView
        private val bellImageView = binding.bellImageView
        private val recentTextView = binding.recentTextView
        private val hotTextView = binding.hotTextView
        private val noneTextView = binding.noneTextView
        private val incidentIdTextView = binding.incidentIdTextView
        private val otherLayout = binding.otherLayout
        private val numOfOtherTextView = binding.numOfOtherTextView
        private val lineBottomView = binding.lineBottomView
        private val chainsawLayout = binding.chainsawLayout
        private val numOfChainsawTextView = binding.numOfChainsawTextView
        private val gunLayout = binding.gunLayout
        private val numOfGunTextView = binding.numOfGunTextView
        private val iconTypeImageView = binding.iconTypeImageView
        private val typeTextView = binding.typeTextView

        fun bind(stream: Stream) {
            // Reset
            listOf(recentTextView, hotTextView, timeTextView, bellImageView, chainsawLayout, gunLayout, otherLayout).forEach {
                it.visibility = View.GONE
            }
            noneTextView.visibility = View.VISIBLE

            // Stream level
            guardianName.text = stream.name

            // Incident level
            val incident = stream.lastIncident ?: return
            noneTextView.visibility = View.GONE
            incidentIdTextView.visibility = View.VISIBLE
            incidentIdTextView.text = stream.lastIncident?.let { itemView.context.getString(R.string.incident_ref, it.ref) } ?: "-"

            iconTypeImageView.setImageResource(
                if (stream.guardianType == GuardianType.CELL.value) R.drawable.ic_signal_cellular_alt
                else R.drawable.ic_satellite_alt
            )
            typeTextView.text = itemView.context.getString(if (stream.guardianType == GuardianType.CELL.value) R.string.cell else R.string.satellite)

            val events = incident.events?.sort(Event.EVENT_START)
            lineBottomView.visibility = if (events?.size == 0) View.VISIBLE else View.GONE
            if (events == null || events.size == 0) return
            val timezone = TimeZone.getTimeZone(stream.timezone)
            timeTextView.text = if (timezone == TimeZone.getDefault()) dateRangeFormat(
                itemView.context,
                events.first()!!.start,
                events.last()!!.end,
                timezone
            ) else setShortTimeZone(dateRangeFormat(itemView.context, events.first()!!.start, events.last()!!.end, timezone))
            timeTextView.visibility = View.VISIBLE
            bellImageView.visibility = View.VISIBLE
            val eventsDistinctType = events.distinctBy { a -> a.classification?.value }
            if (eventsDistinctType.isEmpty()) return
            var number = 0

            // sorted by "chainsaw" first, and "gunshot" next, and then other values (for show icon of events)
            val eventsSorted = eventsDistinctType.sortedWith(
                compareBy(
                    { it.classification?.value != GUNSHOT && it.classification?.value != CHAINSAW },
                    { it.classification?.value == GUNSHOT },
                    { it.classification?.value == CHAINSAW }
                )
            )

            stream.tags?.let { tags ->
                if (tags.contains(Stream.TAG_RECENT) && events.isNotEmpty()) recentTextView.visibility = View.VISIBLE
                if (tags.contains(Stream.TAG_HOT) && events.isNotEmpty()) hotTextView.visibility = View.VISIBLE
            }

            eventsSorted.forEachIndexed { index, event ->
                val type = event.classification?.value ?: return
                if (index < 2) {
                    showIconType(type, events)
                } else {
                    otherLayout.visibility = View.VISIBLE
                    number += getNumberOfEventByType(events, type).toInt()
                    numOfOtherTextView.text = (number).toString()
                }
            }
        }

        private fun showIconType(type: String, events: List<Event>) {
            when (type) {
                GUNSHOT -> {
                    gunLayout.visibility = View.VISIBLE
                    numOfGunTextView.text = getNumberOfEventByType(events, type)
                }
                CHAINSAW -> {
                    chainsawLayout.visibility = View.VISIBLE
                    numOfChainsawTextView.text = getNumberOfEventByType(events, type)
                }
                else -> {
                    otherLayout.visibility = View.VISIBLE
                    numOfOtherTextView.text = getNumberOfEventByType(events, type)
                }
            }
        }

        private fun getNumberOfEventByType(events: List<Event>, type: String): String {
            return events.filter { a -> a.classification?.value == type }.size.toString()
        }
    }

    val Number.toPx
        get() = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            this.toFloat(),
            Resources.getSystem().displayMetrics
        ).toInt()

    companion object {
        private const val MINUTE = 60L * 1000L
        private const val HOUR = 60L * MINUTE
        private const val DAY = 24L * HOUR
        const val GUNSHOT = "gunshot"
        const val CHAINSAW = "chainsaw"
    }
}
