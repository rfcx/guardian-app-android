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
            listOf(chainsawLayout, gunLayout, peopleLayout, otherLayout).forEach { it.visibility = View.GONE }

            guardianName.text = stream.name
            // val alerts = stream.alerts.sortedBy { a -> a.start }
            // recentTextView.visibility =
            //     if (alerts.isNotEmpty() && System.currentTimeMillis() - alerts.last().start.time <= 6 * HOUR) View.VISIBLE else View.GONE
            // hotTextView.visibility = if (alerts.size > 10) View.VISIBLE else View.GONE
            // timeTextView.text = stream.eventTime

            val hasNoEvents = true // stream.eventSize == 0
            timeTextView.visibility = if (hasNoEvents) View.GONE else View.VISIBLE
            bellImageView.visibility = if (hasNoEvents) View.GONE else View.VISIBLE
            noneTextView.visibility = if (hasNoEvents) View.VISIBLE else View.GONE
            incidentIdTextView.visibility = if (hasNoEvents) View.GONE else View.VISIBLE
            guardianNameTextView.setPadding(
                16.toPx,
                16.toPx,
                if (hasNoEvents) 16.toPx else 0.toPx,
                if (hasNoEvents) 16.toPx else 10.toPx
            )
            incidentIdTextView.text = stream.lastIncident?.let { itemView.context.getString(R.string.incident_ref, it.ref) } ?: "-"

            // val typeOfAlert = alerts.distinctBy { a -> a.classification?.value }
            // if (typeOfAlert.isEmpty()) return
            //
            // var number = 0
            // typeOfAlert.forEachIndexed { index, alert ->
            //     val type = alert.classification?.value ?: return
            //     if (index < 2) {
            //         showIconType(type, alerts)
            //     } else {
            //         otherLayout.visibility = View.VISIBLE
            //         number += getNumberOfAlertByType(alerts, type).toInt()
            //         numOfOtherTextView.text = (number).toString()
            //     }
            // }
        }

        private fun showIconType(type: String, events: List<Event>) {
            when (type) {
                GUNSHOT -> {
                    gunLayout.visibility = View.VISIBLE
                    numOfGunTextView.text = getNumberOfAlertByType(events, type)
                }
                HUMAN_VOICE -> {
                    peopleLayout.visibility = View.VISIBLE
                    numOfPeopleTextView.text = getNumberOfAlertByType(events, type)
                }
                CHAINSAW -> {
                    chainsawLayout.visibility = View.VISIBLE
                    numOfChainsawTextView.text = getNumberOfAlertByType(events, type)
                }
            }
        }

        private fun getNumberOfAlertByType(events: List<Event>, type: String): String {
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
