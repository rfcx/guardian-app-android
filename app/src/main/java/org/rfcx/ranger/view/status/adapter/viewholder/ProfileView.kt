package org.rfcx.ranger.view.status.adapter.viewholder

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import org.rfcx.ranger.databinding.ItemHeaderProfileBinding
import org.rfcx.ranger.util.Preferences
import org.rfcx.ranger.util.getUserProfile
import org.rfcx.ranger.util.saveUserProfile
import org.rfcx.ranger.util.setImageProfile
import org.rfcx.ranger.view.profile.ProfileFragment.Companion.LOGIN_WITH_EMAIL
import org.rfcx.ranger.view.status.StatusFragmentListener
import org.rfcx.ranger.view.status.adapter.StatusAdapter

class ProfileView(private val binding: ItemHeaderProfileBinding, private val listener: StatusFragmentListener?) : RecyclerView.ViewHolder(binding.root) {
	
	fun bind(item: StatusAdapter.ProfileItem) {
		binding.profile = item
		binding.onTrackLocationSwitchClick = View.OnClickListener {
			listener?.enableTracking(!item.isLocationTracking)
		}
		
		val loginWith = binding.root.context.let { Preferences.getInstance(it).getString(Preferences.LOGIN_WITH) }
		if (loginWith == LOGIN_WITH_EMAIL) {
			binding.userProfileImageView.visibility = View.VISIBLE
			binding.userProfileImageView.setImageProfile(binding.root.context.getUserProfile())
			
		} else {
			binding.userProfileImageView.visibility = View.GONE
		}
		
		binding.executePendingBindings()
	}
}