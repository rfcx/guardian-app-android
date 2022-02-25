package org.rfcx.incidents.view.events.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.rfcx.incidents.R
import org.rfcx.incidents.databinding.ItemEventBinding
import org.rfcx.incidents.entity.event.Event
import org.rfcx.incidents.util.toStringWithTimeZone
import org.rfcx.incidents.util.toTimeSinceStringAlternativeTimeAgo
import java.util.TimeZone

class EventItemAdapter(private val onClickListener: (Event) -> Unit) :
    RecyclerView.Adapter<EventItemAdapter.EventItemViewHolder>() {
    var items: List<Event> = arrayListOf()
        @SuppressLint("NotifyDataSetChanged")
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    var timeZone: TimeZone = TimeZone.getDefault()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventItemViewHolder {
        val binding = ItemEventBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return EventItemViewHolder(binding)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: EventItemViewHolder, position: Int) {
        holder.bind(items[position])
        holder.itemView.setOnClickListener {
            onClickListener(items[position])
        }
    }

    inner class EventItemViewHolder(binding: ItemEventBinding) : RecyclerView.ViewHolder(binding.root) {
        private val typeTextView = binding.typeTextView
        private val dateTextView = binding.dateTextView
        private val numberOfEventsImageView = binding.numberOfEventsImageView

        fun bind(item: Event) {
            dateTextView.text = if (timeZone == TimeZone.getDefault()) item.start.toTimeSinceStringAlternativeTimeAgo(
                itemView.context,
                timeZone
            ) else item.start.toStringWithTimeZone(timeZone)
            typeTextView.text = item.classification?.title
            val imageIcon = when (item.classification?.value) {
                StreamAdapter.GUNSHOT -> R.drawable.ic_gun
                StreamAdapter.CHAINSAW -> R.drawable.ic_chainsaw
                StreamAdapter.VEHICLE -> R.drawable.ic_vehicle
                StreamAdapter.VOICE -> R.drawable.ic_voice
                StreamAdapter.DOG_BARK -> R.drawable.ic_dog_bark
                else -> R.drawable.ic_report
            }
            numberOfEventsImageView.setImageResource(imageIcon)
        }
    }
}
