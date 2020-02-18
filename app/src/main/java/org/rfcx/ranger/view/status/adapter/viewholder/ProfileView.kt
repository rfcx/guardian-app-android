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
import org.rfcx.ranger.view.status.StatusFragmentListener
import org.rfcx.ranger.view.status.adapter.StatusAdapter

class ProfileView(private val binding: ItemHeaderProfileBinding, private val listener: StatusFragmentListener?) : RecyclerView.ViewHolder(binding.root) {
	
	fun bind(item: StatusAdapter.ProfileItem) {
		binding.profile = item
		binding.onTrackLocationSwitchClick = View.OnClickListener {
			listener?.enableTracking(!item.isLocationTracking)
		}
		
		val loginWith = binding.root.context.let { Preferences.getInstance(it).getString(Preferences.LOGIN_WITH) }
		if (loginWith == "email") {
			binding.linearLayout.visibility = View.VISIBLE
			
			if (binding.root.context.getUserProfile() == "null") {
				binding.root.context.saveUserProfile()
			}
			
			val imageView = ImageView(binding.root.context)
			Glide.with(binding.root.context).load(binding.root.context.getUserProfile()).diskCacheStrategy(DiskCacheStrategy.NONE).skipMemoryCache(true).apply(RequestOptions.circleCropTransform()).into(imageView)
			binding.linearLayout.addView(imageView)
			
		} else {
			binding.linearLayout.visibility = View.GONE
		}
		
		binding.executePendingBindings()
	}
}