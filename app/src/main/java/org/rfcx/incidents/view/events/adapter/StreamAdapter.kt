package org.rfcx.incidents.view.events.adapter

import android.content.Context
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
        private val vehicleLayout = binding.vehicleLayout
        private val numOfVehicleTextView = binding.numOfVehicleTextView
        private val voiceLayout = binding.voiceLayout
        private val numOfVoiceTextView = binding.numOfVoiceTextView
        private val dogBarkLayout = binding.dogBarkLayout
        private val numOfDogBarkTextView = binding.numOfDogBarkTextView
        private val elephantLayout = binding.elephantLayout
        private val numOfElephantTextView = binding.numOfElephantTextView
        private val fireLayout = binding.fireLayout
        private val numOfFireTextView = binding.numOfFireTextView
        private val reportImageView = binding.reportImageView
        private val createByTextView = binding.createByTextView

        fun bind(stream: Stream) {
            // Reset
            listOf(
                recentTextView,
                hotTextView,
                timeTextView,
                bellImageView,
                createByTextView,
                reportImageView,
                chainsawLayout,
                gunLayout,
                otherLayout,
                vehicleLayout,
                voiceLayout,
                dogBarkLayout,
                elephantLayout,
                fireLayout
            ).forEach {
                it.visibility = View.GONE
            }
            noneTextView.visibility = View.VISIBLE

            // Stream level
            guardianName.text = stream.name

            // Incident level
            val incident = stream.lastIncident ?: return
            noneTextView.visibility = View.GONE
            incidentIdTextView.visibility = View.VISIBLE

            reportImageView.visibility = if (incident.responses?.isNotEmpty() == true) View.VISIBLE else View.GONE
            createByTextView.visibility = if (incident.responses?.isNotEmpty() == true) View.VISIBLE else View.GONE
            incident.responses?.let { res ->
                val userText = when {
                    res.getOrNull(0)?.firstname.isNullOrBlank() -> {
                        itemView.context.getString(R.string.response)
                    }
                    res.size == 1 -> {
                        itemView.context.getString(R.string.response_by) + " " + res[0]?.firstname.toString().firstCharUppercase
                    }
                    else -> {
                        setCreatedByText(itemView.context, res.map { u -> u?.firstname ?: "" })
                    }
                }

                createByTextView.text = userText
            }

            incidentIdTextView.text = itemView.context.getString(R.string.incident_ref, incident.ref)
            iconTypeImageView.visibility = if (stream.guardianType == null || stream.guardianType == "unknown") View.GONE else View.VISIBLE
            typeTextView.visibility = if (stream.guardianType == null || stream.guardianType == "unknown") View.GONE else View.VISIBLE
            iconTypeImageView.setImageResource(
                if (stream.guardianType == GuardianType.CELL.value) R.drawable.ic_signal_cellular_alt
                else R.drawable.ic_satellite_alt
            )
            val typeStr = if (stream.guardianType == GuardianType.CELL.value) R.string.cell else R.string.satellite
            typeTextView.text = itemView.context.getString(typeStr)
            val events = incident.events?.sort(Event.EVENT_START)
            lineBottomView.visibility = if (events?.size == 0) View.VISIBLE else View.GONE
            if (events == null || events.size == 0) return
            val timezone = TimeZone.getTimeZone(stream.timezoneRaw)
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

            val value = listOf(CHAINSAW, GUNSHOT, VEHICLE, VOICE, FIRE, DOG_BARK, ELEPHANT)
            var counts = 0
            value.forEach { v ->
                if (events.any { a -> a.classification?.value == v }) {
                    if (counts < 2) {
                        showIconType(v, events)
                    } else {
                        otherLayout.visibility = View.VISIBLE
                        number += getNumberOfEventByType(events, v).toInt()
                        numOfOtherTextView.text = (number).toString()
                    }
                    counts += 1
                }
            }

            val notInDefaultType = events.filter { e -> !value.contains(e.classification?.value) }
            if (notInDefaultType.isNotEmpty()) {
                otherLayout.visibility = View.VISIBLE
                number += notInDefaultType.size
                numOfOtherTextView.text = (number).toString()
            }
            stream.tags?.let { tags ->
                if (tags.contains(Stream.TAG_RECENT) && events.isNotEmpty()) recentTextView.visibility = View.VISIBLE
                if (tags.contains(Stream.TAG_HOT) && events.isNotEmpty()) hotTextView.visibility = View.VISIBLE
            }
        }

        private fun showIconType(type: String, events: List<Event>) {
            when (type) {
                GUNSHOT -> {
                    gunLayout.visibility = View.VISIBLE
                    numOfGunTextView.text = getNumberOfEventByType(events, type).toString()
                }
                CHAINSAW -> {
                    chainsawLayout.visibility = View.VISIBLE
                    numOfChainsawTextView.text = getNumberOfEventByType(events, type).toString()
                }
                VEHICLE -> {
                    vehicleLayout.visibility = View.VISIBLE
                    numOfVehicleTextView.text = getNumberOfEventByType(events, type).toString()
                }
                VOICE -> {
                    voiceLayout.visibility = View.VISIBLE
                    numOfVoiceTextView.text = getNumberOfEventByType(events, type).toString()
                }
                DOG_BARK -> {
                    dogBarkLayout.visibility = View.VISIBLE
                    numOfDogBarkTextView.text = getNumberOfEventByType(events, type).toString()
                }
                ELEPHANT -> {
                    elephantLayout.visibility = View.VISIBLE
                    numOfElephantTextView.text = getNumberOfEventByType(events, type).toString()
                }
                FIRE -> {
                    fireLayout.visibility = View.VISIBLE
                    numOfFireTextView.text = getNumberOfEventByType(events, type).toString()
                }
                else -> {
                    otherLayout.visibility = View.VISIBLE
                    numOfOtherTextView.text = getNumberOfEventByType(events, type).toString()
                }
            }
        }

        private fun getNumberOfEventByType(events: List<Event>, type: String): Int {
            return events.filter { a -> a.classification?.value == type }.size
        }
    }

    fun setCreatedByText(context: Context, users: List<String>): String {
        val userFilter = users.toCheckDuplicate()
        var createByText = ""
        userFilter.forEach { firstname ->
            createByText += when (firstname) {
                userFilter.first() -> firstname
                userFilter.last() -> " " + context.getString(R.string.and) + " " + firstname
                else -> ", $firstname"
            }
        }
        return if (createByText.isBlank()) {
            "${users.size} ${context.getString(R.string.responses)}"
        } else {
            "${users.size} ${context.getString(R.string.responses_by)} $createByText"
        }
    }

    private fun List<String>.toCheckDuplicate(): ArrayList<String> {
        val values = arrayListOf<String>()
        this.forEach { s ->
            if (!values.contains(s.firstCharUppercase)) {
                values.add(s.firstCharUppercase)
            }
        }
        return values
    }

    val String.firstCharUppercase
        get() = this.replaceFirstChar { it.uppercase() }

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
        const val DOG_BARK = "dog_bark"
        const val VEHICLE = "vehicle"
        const val VOICE = "humanvoice"
        const val ELEPHANT = "elephant"
        const val FIRE = "fire"
    }
}
