package org.rfcx.incidents.view.guardian.checklist.site

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.rfcx.incidents.R
import org.rfcx.incidents.databinding.ItemSiteBinding
import org.rfcx.incidents.entity.stream.Stream
import org.rfcx.incidents.util.setFormatLabel

class SiteAdapter(private val itemClickListener: (Stream, Boolean) -> Unit) :
    RecyclerView.Adapter<SiteAdapter.SiteAdapterViewHolder>() {

    private lateinit var siteBinding: ItemSiteBinding

    var items: List<SiteWithDistanceItem> = listOf()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    var isNewSite = false

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): SiteAdapter.SiteAdapterViewHolder {
        siteBinding = ItemSiteBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SiteAdapterViewHolder(siteBinding)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: SiteAdapter.SiteAdapterViewHolder, position: Int) {
        val site = items[position]
        holder.bind(site)
        holder.itemView.setOnClickListener {
            this.itemClickListener(site.stream, site.stream.externalId == null)
        }
    }

    fun setFilter(newList: List<SiteWithDistanceItem>?) {
        items = newList ?: listOf()
        notifyDataSetChanged()
    }

    inner class SiteAdapterViewHolder(private val binding: ItemSiteBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(site: SiteWithDistanceItem) {
            binding.createdSiteNameTextView.text =
                itemView.context.getString(R.string.create_site, site.stream.name)
            binding.siteNameTextView.text = site.stream.name

            binding.createdSiteNameTextView.visibility =
                if (site.stream.externalId == null) View.VISIBLE else View.GONE
            binding.siteNameTextView.visibility = if (site.stream.externalId != null) View.VISIBLE else View.GONE

            // binding.detailTextView.text = site.date?.toTimeSinceStringAlternativeTimeAgo(itemView.context)
            //     ?: itemView.context.getString(R.string.no_deployments)
            if (site.distance != null) {
                binding.distanceTextView.visibility = View.VISIBLE
                binding.distanceTextView.text = site.distance.setFormatLabel()
            } else {
                binding.distanceTextView.visibility = View.GONE
            }
            setDistanceAndIconAdd(site.stream.externalId == null)
        }

        private fun setDistanceAndIconAdd(boolean: Boolean) {
            binding.distanceTextView.visibility = if (boolean) View.GONE else View.VISIBLE
            binding.detailTextView.visibility = if (boolean) View.GONE else View.VISIBLE
            binding.iconAddImageView.visibility = if (boolean) View.VISIBLE else View.GONE
        }
    }
}

data class SiteWithDistanceItem(val stream: Stream, val distance: Float? = 0F)

