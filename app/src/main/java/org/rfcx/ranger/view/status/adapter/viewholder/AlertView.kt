package org.rfcx.ranger.view.status.adapter.viewholder

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.View
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import org.rfcx.ranger.R
import org.rfcx.ranger.databinding.ItemStatusAlertBinding
import org.rfcx.ranger.util.EventItem
import org.rfcx.ranger.util.getNameEmail
import org.rfcx.ranger.view.status.StatusFragmentListener
import org.rfcx.ranger.view.status.adapter.StatusAdapter

class AlertView(private val binding: ItemStatusAlertBinding, private val listener: StatusFragmentListener?) : RecyclerView.ViewHolder(binding.root) {
	fun bind(item: StatusAdapter.AlertItem) {
		binding.alertItem = item
		binding.context = binding.root.context
		binding.reviewedTextView.visibility = View.VISIBLE
		
		binding.agreeTextView.text = item.alert.confirmedCount.toString()
		binding.rejectTextView.text = item.alert.rejectedCount.toString()
		
		if (item.alert.firstNameReviewer.isNotBlank() || item.state !== StatusAdapter.AlertItem.State.NONE) {
			binding.nameReviewerTextView.visibility = View.VISIBLE
		} else {
			binding.nameReviewerTextView.visibility = View.INVISIBLE
		}
		
		if (item.state !== StatusAdapter.AlertItem.State.NONE) {
			binding.nameReviewerTextView.text = binding.root.context.getNameEmail()
		} else if (item.alert.firstNameReviewer.isNotBlank()) {
			binding.nameReviewerTextView.text = item.alert.firstNameReviewer
		}
		
		when {
			item.state == StatusAdapter.AlertItem.State.CONFIRM -> {
				binding.linearLayout.visibility = View.VISIBLE
				
				binding.agreeImageView.background = binding.root.context.getImage(R.drawable.bg_circle_red)
				binding.agreeImageView.setImageDrawable(binding.root.context.getImage(R.drawable.ic_confirm_event_white))
				
				binding.rejectImageView.setImageDrawable(binding.root.context.getImage(R.drawable.ic_reject_event_gray))
				binding.rejectImageView.setBackgroundColor(binding.root.context.getBackgroundColor(R.color.transparent))
				
			}
			item.state == StatusAdapter.AlertItem.State.REJECT -> {
				binding.linearLayout.visibility = View.VISIBLE
				
				binding.rejectImageView.background = binding.root.context.getImage(R.drawable.bg_circle_grey)
				binding.rejectImageView.setImageDrawable(binding.root.context.getImage(R.drawable.ic_reject_event_white))
				
				binding.agreeImageView.setImageDrawable(binding.root.context.getImage(R.drawable.ic_confirm_event_gray))
				binding.agreeImageView.setBackgroundColor(binding.root.context.getBackgroundColor(R.color.transparent))
				
			}
			item.state == StatusAdapter.AlertItem.State.NONE -> {
				binding.linearLayout.visibility = View.INVISIBLE
				binding.agreeImageView.setBackgroundColor(binding.root.context.getBackgroundColor(R.color.transparent))
				binding.rejectImageView.setBackgroundColor(binding.root.context.getBackgroundColor(R.color.transparent))
			}
		}
		
		binding.onClickedAlertItem = View.OnClickListener {
			var state = EventItem.State.NONE
			
			if (item.state == StatusAdapter.AlertItem.State.REJECT) {
				state = EventItem.State.REJECT
			} else if (item.state == StatusAdapter.AlertItem.State.CONFIRM) {
				state = EventItem.State.CONFIRM
			}
			listener?.onClickedAlertItem(item.alert, state)
		}
		binding.executePendingBindings()
	}
	
	private fun Context.getImage(res: Int): Drawable? {
		return ContextCompat.getDrawable(this, res)
	}
	
	private fun Context.getBackgroundColor(res: Int): Int {
		return ContextCompat.getColor(this, res)
	}
}