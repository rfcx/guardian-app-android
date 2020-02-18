package org.rfcx.ranger.view.status.adapter.viewholder

import android.view.View
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import kotlinx.android.synthetic.main.fragment_profile.*
import org.rfcx.ranger.databinding.ItemHeaderProfileBinding
import org.rfcx.ranger.util.Preferences
import org.rfcx.ranger.util.getUserProfile
import org.rfcx.ranger.util.saveUserProfile
import org.rfcx.ranger.util.setImageProfile
import org.rfcx.ranger.view.status.StatusFragmentListener
import org.rfcx.ranger.view.status.adapter.StatusAdapter

class ProfileView(private val binding: ItemHeaderProfileBinding, private val listener: StatusFragmentListener?) : RecyclerView.ViewHolder(binding.root) {
	
	fun bind(item: StatusAdapter.ProfileItem) {
		binding.profile = item
		binding.onTrackLocationSwitchClick = View.OnClickListener {
			listener?.enableTracking(!item.isLocationTracking)
		}
		
		val loginWith = binding.root.context.let { Preferences.getInstance(it).getString(Preferences.LOGIN_WITH) }
		if (loginWith == "auth0") {
			binding.userProfileImageView.visibility = View.VISIBLE
			
			if (binding.root.context.getUserProfile() == "null") {
				binding.root.context.saveUserProfile()
			}
			
			binding.userProfileImageView.setImageProfile(binding.root.context.getUserProfile())
			
		} else {
			binding.userProfileImageView.visibility = View.GONE
		}
		
		binding.executePendingBindings()
	}
}