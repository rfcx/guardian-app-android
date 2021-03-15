package org.rfcx.ranger.view.status.adapter.viewholder

import android.annotation.SuppressLint
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
	@SuppressLint("DefaultLocale")
	fun bind(item: StatusAdapter.AlertItem) {
		binding.alertItem = item
		binding.context = binding.root.context
		binding.reviewedTextView.visibility = View.VISIBLE
		binding.confirmCount = item.event.confirmedCount.toString()
		binding.rejectCount = item.event.rejectedCount.toString()
		
		when (item.state) {
			StatusAdapter.AlertItem.State.CONFIRM -> {
				binding.linearLayout.visibility = View.VISIBLE
				
				binding.agreeImageView.background = binding.root.context.getImage(R.drawable.bg_circle_red)
				binding.agreeImageView.setImageDrawable(binding.root.context.getImage(R.drawable.ic_confirm_event_white))
				
				binding.rejectImageView.setImageDrawable(binding.root.context.getImage(R.drawable.ic_reject_event_gray))
				binding.rejectImageView.setBackgroundColor(binding.root.context.getBackgroundColor(R.color.transparent))
				
				val confirmCount = if (item.event.confirmedCount > 0) item.event.confirmedCount  else 1
				binding.confirmCount = confirmCount.toString()
			}
			StatusAdapter.AlertItem.State.REJECT -> {
				binding.linearLayout.visibility = View.VISIBLE
				
				binding.rejectImageView.background = binding.root.context.getImage(R.drawable.bg_circle_grey)
				binding.rejectImageView.setImageDrawable(binding.root.context.getImage(R.drawable.ic_reject_event_white))
				
				binding.agreeImageView.setImageDrawable(binding.root.context.getImage(R.drawable.ic_confirm_event_gray))
				binding.agreeImageView.setBackgroundColor(binding.root.context.getBackgroundColor(R.color.transparent))
				
				val rejectedCount = if (item.event.rejectedCount > 0) item.event.rejectedCount  else 1
				binding.rejectCount = rejectedCount.toString()
			}
			StatusAdapter.AlertItem.State.NONE -> {
				binding.linearLayout.visibility = View.INVISIBLE
				binding.agreeImageView.setBackgroundColor(binding.root.context.getBackgroundColor(R.color.transparent))
				binding.rejectImageView.setBackgroundColor(binding.root.context.getBackgroundColor(R.color.transparent))
			}
		}
		
		binding.onClickedAlertItem = View.OnClickListener {
			if (!item.event.audioId.isBlank()) {
				val state = when (item.state) {
					StatusAdapter.AlertItem.State.REJECT -> EventItem.State.REJECT
					StatusAdapter.AlertItem.State.CONFIRM -> EventItem.State.CONFIRM
					else -> EventItem.State.NONE
				}
				listener?.onClickedAlertItem(item.event, state)
			}
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
