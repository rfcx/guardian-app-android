package org.rfcx.incidents.view.guardian.connect

import android.net.wifi.ScanResult
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import org.rfcx.incidents.R
import org.rfcx.incidents.databinding.ItemEventBinding
import org.rfcx.incidents.databinding.ItemGuardianHotspotBinding

class GuardianHotspotAdapter(private val onHotspotClickListener: (ScanResult) -> Unit) : RecyclerView.Adapter<GuardianHotspotAdapter.GuardianHotspotViewHolder>() {

    var selectedPosition = -1

    var items: List<ScanResult> = arrayListOf()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GuardianHotspotViewHolder {
        val view = ItemGuardianHotspotBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return GuardianHotspotViewHolder(view)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: GuardianHotspotViewHolder, position: Int) {
        holder.itemView.setBackgroundColor(ContextCompat.getColor(holder.itemView.context, R.color.backgroundColor))

        if (selectedPosition == position) {
            holder.hotspotName.apply {
                setTextColor(ContextCompat.getColor(this.context, R.color.colorPrimary))
                setCompoundDrawablesWithIntrinsicBounds(
                    0,
                    0,
                    R.drawable.ic_hotspot_selected,
                    0
                )
            }
        } else {
            holder.hotspotName.apply {
                setTextColor(ContextCompat.getColor(this.context, R.color.text_secondary))
                setCompoundDrawablesWithIntrinsicBounds(
                    0,
                    0,
                    0,
                    0
                )
            }
        }
        val hotspot = items[position]
        holder.bind(hotspot)
        holder.itemView.setOnClickListener {
            selectedPosition = position
            notifyDataSetChanged()
            this.onHotspotClickListener(hotspot)
        }
    }

    inner class GuardianHotspotViewHolder(itemView: ItemGuardianHotspotBinding) : RecyclerView.ViewHolder(itemView.root) {
        val hotspotName = itemView.hotspotNameTextView

        fun bind(hotspot: ScanResult) {
            hotspotName.text = hotspot.SSID
        }
    }
}
