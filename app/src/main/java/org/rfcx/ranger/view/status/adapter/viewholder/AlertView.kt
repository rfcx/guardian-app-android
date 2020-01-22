package org.rfcx.ranger.view.status.adapter.viewholder

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.View
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import org.rfcx.ranger.R
import org.rfcx.ranger.databinding.ItemStatusAlertBinding
import org.rfcx.ranger.view.status.StatusFragmentListener
import org.rfcx.ranger.view.status.adapter.StatusAdapter

class AlertView(private val binding: ItemStatusAlertBinding, private val listener: StatusFragmentListener?) : RecyclerView.ViewHolder(binding.root) {
	fun bind(item: StatusAdapter.AlertItem) {
		binding.alertItem = item
		binding.context = binding.root.context
		
		when {
			item.state == StatusAdapter.AlertItem.State.CONFIRM -> {
				binding.linearLayout.visibility = View.VISIBLE
				binding.reviewedTextView.visibility = View.VISIBLE
				binding.nameReviewerTextView.visibility = View.VISIBLE
				binding.tvAlertTimeAgoAfterReview.visibility = View.VISIBLE
				binding.tvAlertTimeAgo.visibility = View.INVISIBLE
			}
			item.state == StatusAdapter.AlertItem.State.REJECT -> {
				binding.linearLayout.visibility = View.VISIBLE
				binding.reviewedTextView.visibility = View.VISIBLE
				binding.nameReviewerTextView.visibility = View.VISIBLE
				binding.tvAlertTimeAgoAfterReview.visibility = View.VISIBLE
				binding.tvAlertTimeAgo.visibility = View.INVISIBLE
			}
			item.state == StatusAdapter.AlertItem.State.NONE -> {
				binding.linearLayout.visibility = View.INVISIBLE
				binding.reviewedTextView.visibility = View.INVISIBLE
				binding.nameReviewerTextView.visibility = View.INVISIBLE
				binding.tvAlertTimeAgoAfterReview.visibility = View.INVISIBLE
				binding.tvAlertTimeAgo.visibility = View.VISIBLE
			}
		}

		if (item.state != StatusAdapter.AlertItem.State.NONE) {
			binding.agreeImageView.background = binding.root.context.getImage(R.drawable.bg_circle_red)
		}
		
		binding.onClickedAlertItem = View.OnClickListener {
			listener?.onClickedAlertItem(item.alert)
		}
		binding.executePendingBindings()
	}
	
	private fun Context.getImage(res: Int): Drawable? {
		return ContextCompat.getDrawable(this, res)
	}
}