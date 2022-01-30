package org.rfcx.incidents.view.events.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.rfcx.incidents.databinding.ItemEventBinding
import org.rfcx.incidents.entity.alert.Alert
import org.rfcx.incidents.util.toTimeSinceStringAlternativeTimeAgo

class AlertItemAdapter(private val onClickListener: (Alert) -> Unit) :
    RecyclerView.Adapter<AlertItemAdapter.AlertItemViewHolder>() {
    var items: List<Alert> = arrayListOf()
        @SuppressLint("NotifyDataSetChanged")
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlertItemViewHolder {
        val binding = ItemEventBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AlertItemViewHolder(binding)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: AlertItemViewHolder, position: Int) {
        holder.bind(items[position])
        holder.itemView.setOnClickListener {
            onClickListener(items[position])
        }
    }

    inner class AlertItemViewHolder(binding: ItemEventBinding) : RecyclerView.ViewHolder(binding.root) {
        private val typeTextView = binding.typeTextView
        private val dateTextView = binding.dateTextView

        fun bind(item: Alert) {
            dateTextView.text = item.start.toTimeSinceStringAlternativeTimeAgo(itemView.context)
            typeTextView.text = item.classification?.title
        }
    }
}
