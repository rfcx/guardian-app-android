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
import org.rfcx.incidents.entity.stream.Stream
import org.rfcx.incidents.util.dateRangeFormat
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
        private val guardianNameTextView = binding.guardianNameTextView
        private val chainsawLayout = binding.chainsawLayout
        private val numOfChainsawTextView = binding.numOfChainsawTextView
        private val gunLayout = binding.gunLayout
        private val numOfGunTextView = binding.numOfGunTextView
        private val peopleLayout = binding.peopleLayout
        private val numOfPeopleTextView = binding.numOfPeopleTextView

        fun bind(stream: Stream) {
            // Reset
            listOf(recentTextView, hotTextView, timeTextView, bellImageView, chainsawLayout, gunLayout, peopleLayout, otherLayout).forEach {
                it.visibility = View.GONE
            }
            noneTextView.visibility = View.VISIBLE

            // Stream level
            guardianName.text = stream.name
            stream.tags?.let { tags ->
                if (tags.contains("recent")) recentTextView.visibility = View.VISIBLE
                if (tags.contains("hot")) hotTextView.visibility = View.VISIBLE
            }

            // Incident level
            val incident = stream.lastIncident ?: return
            noneTextView.visibility = View.GONE
            incidentIdTextView.visibility = View.VISIBLE
            incidentIdTextView.text = stream.lastIncident?.let { itemView.context.getString(R.string.incident_ref, it.ref) } ?: "-"

            val events = incident.events
            if (events == null || events.size == 0) return
            timeTextView.text = dateRangeFormat(itemView.context, events.first()!!.start, events.last()!!.end, TimeZone.getTimeZone(stream.timezone))
            timeTextView.visibility = View.VISIBLE
            bellImageView.visibility = View.VISIBLE
            val eventsDistinctType = events.distinctBy { a -> a.classification?.value }
            if (eventsDistinctType.isEmpty()) return

            var number = 0
            eventsDistinctType.forEachIndexed { index, event ->
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
                HUMAN_VOICE -> {
                    peopleLayout.visibility = View.VISIBLE
                    numOfPeopleTextView.text = getNumberOfEventByType(events, type)
                }
                CHAINSAW -> {
                    chainsawLayout.visibility = View.VISIBLE
                    numOfChainsawTextView.text = getNumberOfEventByType(events, type)
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
        private const val GUNSHOT = "gunshot"
        private const val HUMAN_VOICE = "humanvoice"
        private const val CHAINSAW = "chainsaw"
    }
}
