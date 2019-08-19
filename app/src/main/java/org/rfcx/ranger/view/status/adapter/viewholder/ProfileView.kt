package org.rfcx.ranger.view.status.adapter.viewholder

import android.widget.CompoundButton
import androidx.recyclerview.widget.RecyclerView
import org.rfcx.ranger.databinding.ItemHeaderProfileBinding
import org.rfcx.ranger.view.status.StatusFragmentListener
import org.rfcx.ranger.view.status.adapter.StatusAdapter

class ProfileView(private val binding: ItemHeaderProfileBinding, private val listener: StatusFragmentListener?) : RecyclerView.ViewHolder(binding.root) {

    fun bind(item: StatusAdapter.ProfileItem) {
        binding.profile = item
        binding.onTrackingChange = CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
            listener?.enableTracking(isChecked)
        }

        binding.executePendingBindings()
    }
}