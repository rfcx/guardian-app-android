package org.rfcx.ranger.view.status.adapter.viewholder

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import org.rfcx.ranger.databinding.ItemStatusReportBinding
import org.rfcx.ranger.view.status.StatusFragmentListener
import org.rfcx.ranger.view.status.adapter.StatusAdapter

class ReportView(private val binding: ItemStatusReportBinding,private val listener: StatusFragmentListener?) : RecyclerView.ViewHolder(binding.root) {
    fun bind(item: StatusAdapter.ReportItem) {
        binding.reportItem = item
        binding.context = binding.root.context
        // set onclick
        binding.onClickedItem = View.OnClickListener {
            listener?.onClickedReportItem(item.report)
        }
        
        binding.executePendingBindings()
    }
}