package org.rfcx.incidents.view.report.draft

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.rfcx.incidents.databinding.ItemSelectSiteBinding
import org.rfcx.incidents.entity.stream.Stream
import org.rfcx.incidents.util.setFormatLabel
import org.rfcx.incidents.view.SelectSiteItem

class SelectSiteAdapter(private val listener: SelectSiteListener) : RecyclerView.Adapter<SelectSiteAdapter.SelectSiteViewHolder>() {

    var items: List<SelectSiteItem> = arrayListOf()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    inner class SelectSiteViewHolder(binding: ItemSelectSiteBinding) : RecyclerView.ViewHolder(binding.root) {
        private val siteName = binding.selectSiteName
        private val siteDistance = binding.selectSiteDistance
        fun bind(item: SelectSiteItem) {
            siteName.text = item.site.name
            siteDistance.text = item.distance.setFormatLabel()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SelectSiteViewHolder {
        val binding = ItemSelectSiteBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SelectSiteViewHolder(binding)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: SelectSiteViewHolder, position: Int) {
        holder.bind(items[position])
        holder.itemView.setOnClickListener {
            listener.onSiteSelected(items[position].site)
        }
    }
}

interface SelectSiteListener {
    fun onSiteSelected(site: Stream)
}
