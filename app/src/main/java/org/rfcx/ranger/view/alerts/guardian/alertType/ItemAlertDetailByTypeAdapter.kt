package org.rfcx.ranger.view.alerts.guardian.alertType

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_alert_detail_by_type.view.*
import org.rfcx.ranger.R
import org.rfcx.ranger.util.*
import org.rfcx.ranger.view.alerts.adapter.AlertClickListener

class ItemAlertDetailByTypeAdapter(var items: MutableList<EventItem>, val listener: AlertClickListener) : ListAdapter<EventItem, ItemAlertDetailByTypeAdapter.ItemAlertDetailByTypeViewHolder>(ItemAlertDetailByTypeDiffUtil()) {
	
	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemAlertDetailByTypeViewHolder {
		val view = LayoutInflater.from(parent.context).inflate(R.layout.item_alert_detail_by_type, parent, false)
		return ItemAlertDetailByTypeViewHolder(view)
	}
	
	override fun getItemCount(): Int = items.size
	
	override fun onBindViewHolder(holder: ItemAlertDetailByTypeViewHolder, position: Int) {
		val item = items[position]
		holder.bind(item)
		holder.itemView.setOnClickListener { listener.onClickedAlert(item.event, item.state) }
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
		private val context = itemView.context
		private val iconAlert = itemView.ivAlertIcon
		private val tvReviewed = itemView.reviewedTextView
		private val tvNameReviewer = itemView.nameReviewerTextView
		private val tvAgreeValue = itemView.agreeTextView
		private val tvRejectValue = itemView.rejectTextView
		private val ivAgree = itemView.agreeImageView
		private val ivReject = itemView.rejectImageView
		private val linearLayout = itemView.linearLayout
		
		@SuppressLint("DefaultLocale")
		fun bind(item: EventItem) {
			eventsInEventsTextView.text = item.event.beginsAt.toTimeSinceStringAlternativeTimeAgo(itemView.context)
			item.event.value.toEventIcon().let { iconAlert.setImageResource(it) }
			tvAgreeValue.text = item.event.confirmedCount.toString()
			tvRejectValue.text = item.event.rejectedCount.toString()
			tvReviewed.text = context.getString(if (item.event.firstNameReviewer.isNotBlank() || item.state !== EventItem.State.NONE) R.string.last_reviewed_by else R.string.not_have_review)
			tvNameReviewer.text = item.getReviewerName(context)
			tvNameReviewer.visibility = if (item.event.firstNameReviewer.isNotBlank() || item.state !== EventItem.State.NONE) View.VISIBLE else View.INVISIBLE
			linearLayout.visibility = View.INVISIBLE
			
			when (item.state) {
				EventItem.State.CONFIRM -> {
					circleImageView.visibility = View.INVISIBLE
					linearLayout.visibility = View.VISIBLE
					
					ivAgree.background = context.getImage(R.drawable.bg_circle_red)
					ivAgree.setImageDrawable(context.getImage(R.drawable.ic_confirm_event_white))
					
					ivReject.setImageDrawable(context.getImage(R.drawable.ic_reject_event_gray))
					ivReject.setBackgroundColor(context.getBackgroundColor(R.color.transparent))
					
					val confirmCount = if (item.event.confirmedCount > 0) item.event.confirmedCount  else 1
					tvAgreeValue.text = confirmCount.toString()
				}
				EventItem.State.REJECT -> {
					circleImageView.visibility = View.INVISIBLE
					ivReject.background = context.getImage(R.drawable.bg_circle_grey)
					ivReject.setImageDrawable(context.getImage(R.drawable.ic_reject_event_white))
					linearLayout.visibility = View.VISIBLE
					
					ivAgree.setImageDrawable(context.getImage(R.drawable.ic_confirm_event_gray))
					ivAgree.setBackgroundColor(context.getBackgroundColor(R.color.transparent))
					
					val rejectedCount = if (item.event.rejectedCount > 0) item.event.rejectedCount  else 1
					tvRejectValue.text = rejectedCount.toString()
				}
				EventItem.State.NONE -> {
					circleImageView.visibility = View.VISIBLE
					ivAgree.setBackgroundColor(context.getBackgroundColor(R.color.transparent))
					ivReject.setBackgroundColor(context.getBackgroundColor(R.color.transparent))
					ivAgree.setImageDrawable(context.getImage(R.drawable.ic_confirm_event_gray))
					ivReject.setImageDrawable(context.getImage(R.drawable.ic_reject_event_gray))
				}
			}
		}
	}
}