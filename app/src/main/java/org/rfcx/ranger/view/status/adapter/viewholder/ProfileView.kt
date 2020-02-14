package org.rfcx.ranger.view.status.adapter.viewholder

import android.view.View
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import kotlinx.android.synthetic.main.fragment_profile.*
import org.rfcx.ranger.databinding.ItemHeaderProfileBinding
import org.rfcx.ranger.view.status.StatusFragmentListener
import org.rfcx.ranger.view.status.adapter.StatusAdapter

class ProfileView(private val binding: ItemHeaderProfileBinding, private val listener: StatusFragmentListener?) : RecyclerView.ViewHolder(binding.root) {
	
	fun bind(item: StatusAdapter.ProfileItem) {
		binding.profile = item
		binding.onTrackLocationSwitchClick = View.OnClickListener {
			listener?.enableTracking(!item.isLocationTracking)
		}
		
		val imageView = ImageView(binding.root.context)
		Glide.with(binding.root.context).load("https://s.gravatar.com/avatar/a92452eb00e434f762302b6544107ef4?s=480&r=pg&d=https%3A%2F%2Fcdn.auth0.com%2Favatars%2Fra.png").apply(RequestOptions.circleCropTransform()).into(imageView)
		binding.linearLayout.addView(imageView)
		
		binding.executePendingBindings()
	}
}