package org.rfcx.incidents.view.report.deployment

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.rfcx.incidents.R
import org.rfcx.incidents.databinding.ItemDeploymentBinding
import org.rfcx.incidents.databinding.ItemReportBinding
import org.rfcx.incidents.entity.guardian.deployment.Deployment
import org.rfcx.incidents.entity.response.InvestigationType
import org.rfcx.incidents.entity.response.Response
import org.rfcx.incidents.entity.response.SyncState
import org.rfcx.incidents.entity.response.syncImage
import org.rfcx.incidents.util.setDrawableImage
import org.rfcx.incidents.util.setImage
import org.rfcx.incidents.util.toStringWithTimeZone
import org.rfcx.incidents.util.toTimeSinceStringAlternativeTimeAgo
import org.rfcx.incidents.view.report.draft.ReportsAdapter
import java.util.TimeZone

class DeploymentListAdapter() :
    RecyclerView.Adapter<DeploymentListAdapter.DeploymentListViewHolder>() {

    var items: List<Deployment> = arrayListOf()
        @SuppressLint("NotifyDataSetChanged")
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeploymentListAdapter.DeploymentListViewHolder {
        val binding = ItemDeploymentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DeploymentListViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DeploymentListAdapter.DeploymentListViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class DeploymentListViewHolder(binding: ItemDeploymentBinding) : RecyclerView.ViewHolder(binding.root) {

        private val siteName = binding.siteNameTextView
        private val guardianName = binding.guardianNameTextView
        private val dateTextView = binding.dateTextView

        fun bind(item: Deployment) {
            siteName.text = item.stream?.name ?: "sss"
            guardianName.text = item.deploymentKey
            dateTextView.text = item.deployedAt.toStringWithTimeZone(itemView.context, TimeZone.getDefault())
        }
    }
}
