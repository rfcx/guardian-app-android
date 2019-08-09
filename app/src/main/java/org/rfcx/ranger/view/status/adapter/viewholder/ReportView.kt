package org.rfcx.ranger.view.status.adapter.viewholder

import androidx.recyclerview.widget.RecyclerView
import org.rfcx.ranger.databinding.ItemStatusReportBinding
import org.rfcx.ranger.view.status.adapter.StatusAdapter

class ReportView(private val binding: ItemStatusReportBinding) : RecyclerView.ViewHolder(binding.root) {
    fun bind(item: StatusAdapter.ReportItem) {
        binding.reportItem = item
        binding.context = binding.root.context
        binding.executePendingBindings()
    }
}