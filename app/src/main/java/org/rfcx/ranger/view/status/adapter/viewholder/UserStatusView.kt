package org.rfcx.ranger.view.status.adapter.viewholder

import androidx.recyclerview.widget.RecyclerView
import org.rfcx.ranger.databinding.ItemUserStatusBinding
import org.rfcx.ranger.view.status.adapter.StatusAdapter

class UserStatusView(private val binding: ItemUserStatusBinding) : RecyclerView.ViewHolder(binding.root) {
    fun bind(item: StatusAdapter.UserStatusItem) {
        binding.userStatus = item
        binding.executePendingBindings()
    }
}