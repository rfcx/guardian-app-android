package org.rfcx.incidents.view.events.adapter

import android.annotation.SuppressLint
import android.content.res.Resources
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.rfcx.incidents.R
import org.rfcx.incidents.databinding.ItemGuardianBinding
import org.rfcx.incidents.entity.alert.Alert

class StreamItemAdapter(private val onClickListener: (StreamItem) -> Unit) :
    RecyclerView.Adapter<StreamItemAdapter.GuardianItemViewHolder>() {
    var items: List<StreamItem> = arrayListOf()
        @SuppressLint("NotifyDataSetChanged")
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GuardianItemViewHolder {
        val binding = ItemGuardianBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return GuardianItemViewHolder(binding)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: GuardianItemViewHolder, position: Int) {
        holder.bind(items[position])
        holder.itemView.setOnClickListener {
            onClickListener(items[position])
        }
    }

    inner class GuardianItemViewHolder(binding: ItemGuardianBinding) : RecyclerView.ViewHolder(binding.root) {
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

        fun bind(item: StreamItem) {
            // Reset
            listOf(chainsawLayout, gunLayout, peopleLayout, otherLayout).forEach { it.visibility = View.GONE }

            guardianName.text = item.streamName
            val alerts = item.alerts.sortedBy { a -> a.start }
            recentTextView.visibility =
                if (alerts.isNotEmpty() && System.currentTimeMillis() - alerts.last().start.time <= 6 * HOUR) View.VISIBLE else View.GONE
            hotTextView.visibility = if (alerts.size > 10) View.VISIBLE else View.GONE
            timeTextView.text = item.eventTime

            val hasNoEvents = item.eventSize == 0
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
            incidentIdTextView.text = itemView.context.getString(R.string.incident_ref, item.incidentRef.toString())

            val typeOfAlert = alerts.distinctBy { a -> a.classification?.value }
            if (typeOfAlert.isEmpty()) return

            var number = 0
            typeOfAlert.forEachIndexed { index, alert ->
                val type = alert.classification?.value ?: return
                if (index < 2) {
                    showIconType(type, alerts)
                } else {
                    otherLayout.visibility = View.VISIBLE
                    number += getNumberOfAlertByType(alerts, type).toInt()
                    numOfOtherTextView.text = (number).toString()
                }
            }
        }

        private fun showIconType(type: String, alerts: List<Alert>) {
            when (type) {
                GUNSHOT -> {
                    gunLayout.visibility = View.VISIBLE
                    numOfGunTextView.text = getNumberOfAlertByType(alerts, type)
                }
                HUMAN_VOICE -> {
                    peopleLayout.visibility = View.VISIBLE
                    numOfPeopleTextView.text = getNumberOfAlertByType(alerts, type)
                }
                CHAINSAW -> {
                    chainsawLayout.visibility = View.VISIBLE
                    numOfChainsawTextView.text = getNumberOfAlertByType(alerts, type)
                }
            }
        }

        private fun getNumberOfAlertByType(alerts: List<Alert>, type: String): String {
            return alerts.filter { a -> a.classification?.value == type }.size.toString()
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

data class StreamItem(
    val eventSize: Int,
    val incidentRef: Int,
    val distance: Double?,
    val streamName: String,
    val streamId: String,
    val eventTime: String? = null,
    val alerts: List<Alert>
)
