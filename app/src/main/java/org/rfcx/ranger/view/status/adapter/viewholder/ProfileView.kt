package org.rfcx.ranger.view.status.adapter.viewholder

import android.view.View
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import org.rfcx.ranger.databinding.ItemHeaderProfileBinding
import org.rfcx.ranger.util.getUserProfile
import org.rfcx.ranger.view.status.StatusFragmentListener
import org.rfcx.ranger.view.status.adapter.StatusAdapter

class ProfileView(private val binding: ItemHeaderProfileBinding, private val listener: StatusFragmentListener?) : RecyclerView.ViewHolder(binding.root) {
	
	fun bind(item: StatusAdapter.ProfileItem) {
		binding.profile = item
		binding.onTrackLocationSwitchClick = View.OnClickListener {
			listener?.enableTracking(!item.isLocationTracking)
		}
		
		val imageView = ImageView(binding.root.context)
		Glide.with(binding.root.context).load(binding.root.context.getUserProfile()).apply(RequestOptions.circleCropTransform()).into(imageView)
		binding.linearLayout.addView(imageView)
		
		binding.executePendingBindings()
	}
}