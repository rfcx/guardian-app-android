package org.rfcx.ranger.view.status.adapter.viewholder

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import org.rfcx.ranger.databinding.ItemHeaderProfileBinding
import org.rfcx.ranger.util.getUserProfile
import org.rfcx.ranger.util.setImageProfile
import org.rfcx.ranger.view.status.StatusFragmentListener
import org.rfcx.ranger.view.status.adapter.StatusAdapter

class ProfileView(private val binding: ItemHeaderProfileBinding, private val listener: StatusFragmentListener?) : RecyclerView.ViewHolder(binding.root) {
	
	fun bind(item: StatusAdapter.ProfileItem) {
		binding.profile = item
		binding.context = binding.root.context
		binding.onTrackLocationSwitchClick = View.OnClickListener {
			listener?.enableTracking(!item.isLocationTracking)
		}
		
		binding.executePendingBindings()
	}
}