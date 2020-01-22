package org.rfcx.ranger.view.status.adapter.viewholder

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.View
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import org.rfcx.ranger.R
import org.rfcx.ranger.databinding.ItemStatusAlertBinding
import org.rfcx.ranger.util.EventItem
import org.rfcx.ranger.view.status.StatusFragmentListener
import org.rfcx.ranger.view.status.adapter.StatusAdapter

class AlertView(private val binding: ItemStatusAlertBinding, private val listener: StatusFragmentListener?) : RecyclerView.ViewHolder(binding.root) {
	fun bind(item: StatusAdapter.AlertItem) {
		binding.alertItem = item
		binding.context = binding.root.context
		when {
			item.state == StatusAdapter.AlertItem.State.CONFIRM -> {
				binding.agreeImageView.background = binding.root.context.getImage(R.drawable.bg_circle_red)
				binding.rejectImageView.background = binding.root.context.getImage(R.drawable.bg_circle_white)
				binding.linearLayout.visibility = View.VISIBLE
				binding.reviewedTextView.visibility = View.VISIBLE
				binding.nameReviewerTextView.visibility = View.VISIBLE
				//TODO remove
				binding.agreeTextView.text = (item.getConfirmedCount().toInt() + 1).toString()
				binding.rejectTextView.text = item.getRejectedCount()
			}
			item.state == StatusAdapter.AlertItem.State.REJECT -> {
				binding.rejectImageView.background = binding.root.context.getImage(R.drawable.bg_circle_grey)
				binding.agreeImageView.background = binding.root.context.getImage(R.drawable.bg_circle_white)
				binding.linearLayout.visibility = View.VISIBLE
				binding.reviewedTextView.visibility = View.VISIBLE
				binding.nameReviewerTextView.visibility = View.VISIBLE
				//TODO remove
				binding.agreeTextView.text = item.getRejectedCount()
				binding.rejectTextView.text = (item.getRejectedCount().toInt() + 1).toString()
			}
			item.state == StatusAdapter.AlertItem.State.NONE -> {
				binding.linearLayout.visibility = View.INVISIBLE
				binding.reviewedTextView.visibility = View.VISIBLE
				binding.nameReviewerTextView.visibility = View.INVISIBLE
			}
		}
		
		binding.onClickedAlertItem = View.OnClickListener {
			var state = EventItem.State.NONE
			
			if( item.state == StatusAdapter.AlertItem.State.REJECT ) {
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
}