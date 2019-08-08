package org.rfcx.ranger.view.status.adapter.viewholder

import androidx.recyclerview.widget.RecyclerView
import org.rfcx.ranger.databinding.ItemHeaderProfileBinding
import org.rfcx.ranger.view.status.adapter.StatusAdapter

class ProfileView(private val binding: ItemHeaderProfileBinding) : RecyclerView.ViewHolder(binding.root) {

    fun bind(item: StatusAdapter.ProfileItem) {
        binding.profile = item
        binding.executePendingBindings()
    }
}