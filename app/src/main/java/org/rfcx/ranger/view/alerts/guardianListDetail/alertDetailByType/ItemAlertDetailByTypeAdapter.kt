package org.rfcx.ranger.view.alerts.guardianListDetail.alertDetailByType

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_alert_detail_by_type.view.*
import org.rfcx.ranger.R
import org.rfcx.ranger.util.EventItem
import org.rfcx.ranger.util.getUserNickname
import org.rfcx.ranger.util.toEventIcon
import org.rfcx.ranger.util.toTimeSinceStringAlternativeTimeAgo
import org.rfcx.ranger.view.alerts.adapter.AlertClickListener

class ItemAlertDetailByTypeAdapter (var items: MutableList<EventItem>, val listener: AlertClickListener) : ListAdapter<EventItem, ItemAlertDetailByTypeAdapter.ItemAlertDetailByTypeViewHolder>(ItemAlertDetailByTypeDiffUtil()) {
	
	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemAlertDetailByTypeViewHolder {
		val view = LayoutInflater.from(parent.context).inflate(R.layout.item_alert_detail_by_type, parent, false)
		return ItemAlertDetailByTypeViewHolder(view)
	}
	
	override fun getItemCount(): Int = items.size
	
	override fun onBindViewHolder(holder: ItemAlertDetailByTypeViewHolder, position: Int) {
		val item = items[position]
		holder.bind(item)
		holder.itemView.setOnClickListener { listener.onClickedAlert(item.event) }
	}
	
	class ItemAlertDetailByTypeDiffUtil : DiffUtil.ItemCallback<EventItem>() {
		override fun areItemsTheSame(oldItem: EventItem, newItem: EventItem): Boolean {
			return oldItem.state == newItem.state
			
		}
		
		override fun areContentsTheSame(oldItem: EventItem, newItem: EventItem): Boolean {
			return oldItem.event.id == newItem.event.id
					&& oldItem.event.value == newItem.event.value
					&& oldItem.state == newItem.state
		}
	}
	
	inner class ItemAlertDetailByTypeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
		private val eventsInEventsTextView = itemView.eventsInEventsTextView
		private val circleImageView = itemView.circleImageView
		private val reviewedImageView = itemView.reviewedImageView
		private val context = itemView.context

//		private val iconAlert = itemView.ivAlertIcon
//		private val tvReviewed = itemView.reviewedTextView
//		private val tvNameReviewer = itemView.nameReviewerTextView
//		private val tvAgreeValue = itemView.agreeTextView
//		private val tvRejectValue = itemView.rejectTextView
//		private val ivAgree = itemView.agreeImageView
//		private val ivReject = itemView.rejectImageView
		
		var currentEvent: EventItem? = null
		
		fun bind(item: EventItem) {
			eventsInEventsTextView.text = item.event.beginsAt.toTimeSinceStringAlternativeTimeAgo(itemView.context)
//			item.event.value.toEventIcon().let { iconAlert.setImageResource(it) }
//			tvAgreeValue.text = item.event.confirmedCount.toString()
//			tvRejectValue.text = item.event.rejectedCount.toString()
//			val count = item.event.confirmedCount + item.event.rejectedCount
//			tvReviewed.text = context.getString(if (count > 0) R.string.last_reviewed_by else R.string.not_have_review)
//			tvNameReviewer.visibility = if (count > 0) View.VISIBLE else View.INVISIBLE
			this.currentEvent = item
			
			when (item.state) {
				EventItem.State.CONFIRM -> {
					circleImageView.visibility = View.INVISIBLE
					reviewedImageView.background = context.getImage(R.drawable.circle_green_stroke)
					reviewedImageView.setImageDrawable(context.getImage(R.drawable.ic_check))
//					ivAgree.background = context.getImage(R.drawable.bg_circle_green)
//					ivAgree.setImageDrawable(context.getImage(R.drawable.ic_confirm_event_white))
					
//					//TODO: remove
//					if (count == 0) {
//						tvAgreeValue.text = "1"
//						tvReviewed.text = context.getString(R.string.last_reviewed_by)
//						tvNameReviewer.text = context.getUserNickname()
//						tvNameReviewer.visibility = View.VISIBLE
//					}
				}
				EventItem.State.REJECT -> {
					circleImageView.visibility = View.INVISIBLE
					reviewedImageView.background = context.getImage(R.drawable.circle_green_stroke)
					reviewedImageView.setImageDrawable(context.getImage(R.drawable.ic_wrong))
//					ivReject.background = context.getImage(R.drawable.bg_circle_green)
//					ivReject.setImageDrawable(context.getImage(R.drawable.ic_reject_event_white))
					
//					//TODO: remove
//					if (count == 0) {
//						tvRejectValue.text = "1"
//						tvReviewed.text = context.getString(R.string.last_reviewed_by)
//						tvNameReviewer.text = context.getUserNickname()
//						tvNameReviewer.visibility = View.VISIBLE
//					}
				}
				EventItem.State.NONE -> {
					circleImageView.visibility = View.VISIBLE
					reviewedImageView.visibility = View.INVISIBLE
//					ivAgree.setBackgroundColor(context.getBackgroundColor(R.color.transparent))
//					ivReject.setBackgroundColor(context.getBackgroundColor(R.color.transparent))
//					ivAgree.setImageDrawable(context.getImage(R.drawable.ic_confirm_event_gray))
//					ivReject.setImageDrawable(context.getImage(R.drawable.ic_reject_event_gray))
				}
			}
		}
		
		private fun Context.getImage(res: Int): Drawable? {
			return ContextCompat.getDrawable(this, res)
		}
		
		private fun Context.getBackgroundColor(res: Int): Int {
			return ContextCompat.getColor(this, res)
		}
	}
}