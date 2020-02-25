package org.rfcx.ranger.view.alerts.guardian.alertType

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_alert_by_value.view.*
import kotlinx.android.synthetic.main.item_alert_value_load_more.view.*
import org.rfcx.ranger.R
import org.rfcx.ranger.adapter.entity.BaseItem
import org.rfcx.ranger.util.*
import org.rfcx.ranger.view.alerts.adapter.AlertClickListener
import org.rfcx.ranger.view.alerts.adapter.AlertsAdapter

class AlertByValueAdapter(val listener: AlertClickListener, val seeOlder: () -> Unit) : ListAdapter<BaseItem,
		RecyclerView.ViewHolder>(AlertsAdapter.AlertsDiffUtil()) {
	
	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
		val inflater = LayoutInflater.from(parent.context)
		return when (viewType) {
			ITEM_LOAD_MORE_VIEW -> {
				val view = inflater.inflate(R.layout.item_alert_value_load_more, parent, false)
				LoadMoreViewHolder(view)
			}
			else -> {
				val view = inflater.inflate(R.layout.item_alert_by_value, parent, false)
				AlertByValueViewHolder(view)
			}
		}
	}
	
	override fun getItemViewType(position: Int): Int {
		return when (getItem(position)) {
			is LoadMoreItem -> ITEM_LOAD_MORE_VIEW
			else -> ITEM_EVENT_VIEW
		}
	}
	
	override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
		when (val item = getItem(position)) {
			is EventItem -> {
				(holder as AlertByValueViewHolder).bind(item)
				holder.itemView.setOnClickListener { listener.onClickedAlert(item.event, item.state) }
			}
			is LoadMoreItem -> {
				(holder as LoadMoreViewHolder).bind(item)
			}
		}
	}
	
	companion object {
		private const val ITEM_EVENT_VIEW = 1
		private const val ITEM_LOAD_MORE_VIEW = 2
	}
	
	inner class AlertByValueViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
		private val eventsInEventsTextView = itemView.eventsInEventsTextView
		private val circleImageView = itemView.circleImageView
		private val iconAlert = itemView.ivAlertIcon
		private val tvReviewed = itemView.reviewedTextView
		private val tvNameReviewer = itemView.nameReviewerTextView
		private val placeholderIcon = itemView.placeholderIconImageView
		private val tvAgreeValue = itemView.agreeTextView
		private val tvRejectValue = itemView.rejectTextView
		private val ivAgree = itemView.agreeImageView
		private val ivReject = itemView.rejectImageView
		private val linearLayout = itemView.linearLayout
		private val context = itemView.context
		
		@SuppressLint("DefaultLocale")
		fun bind(item: EventItem) {
			eventsInEventsTextView.text = item.event.beginsAt.toTimeSinceStringAlternativeTimeAgo(itemView.context)
			item.event.value.toEventIcon().let { iconAlert.setImageResource(it) }
			tvAgreeValue.text = item.event.confirmedCount.toString()
			tvRejectValue.text = item.event.rejectedCount.toString()
			tvReviewed.text = context.getString(if (item.event.firstNameReviewer.isNotBlank()
					|| item.state !== EventItem.State.NONE) R.string.last_reviewed_by else R.string.not_have_review)
			tvNameReviewer.text = item.getReviewerName(context)
			tvNameReviewer.visibility = if (item.event.firstNameReviewer.isNotBlank() || item.state !== EventItem.State.NONE) View.VISIBLE else View.INVISIBLE
			placeholderIcon.visibility = if (item.event.firstNameReviewer.isNotBlank() || item.state !== EventItem.State.NONE) View.VISIBLE else View.INVISIBLE
			linearLayout.visibility = View.INVISIBLE
			
			when (item.state) {
				EventItem.State.CONFIRM -> {
					circleImageView.visibility = View.INVISIBLE
					linearLayout.visibility = View.VISIBLE
					
					ivAgree.background = context.getImage(R.drawable.bg_circle_red)
					ivAgree.setImageDrawable(context.getImage(R.drawable.ic_confirm_event_white))
					
					ivReject.setImageDrawable(context.getImage(R.drawable.ic_reject_event_gray))
					ivReject.setBackgroundColor(context.getBackgroundColor(R.color.transparent))
					
					val confirmCount = if (item.event.confirmedCount > 0) item.event.confirmedCount else 1
					tvAgreeValue.text = confirmCount.toString()
				}
				EventItem.State.REJECT -> {
					circleImageView.visibility = View.INVISIBLE
					ivReject.background = context.getImage(R.drawable.bg_circle_grey)
					ivReject.setImageDrawable(context.getImage(R.drawable.ic_reject_event_white))
					linearLayout.visibility = View.VISIBLE
					
					ivAgree.setImageDrawable(context.getImage(R.drawable.ic_confirm_event_gray))
					ivAgree.setBackgroundColor(context.getBackgroundColor(R.color.transparent))
					
					val rejectedCount = if (item.event.rejectedCount > 0) item.event.rejectedCount else 1
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
	
	inner class LoadMoreViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
		private val seeOlderTextView = itemView.seeOlderTextView
		private val progressBar = itemView.progressBar
		
		fun bind(item: LoadMoreItem) {
			
			seeOlderTextView.setOnClickListener {
				seeOlder()
			}
			
			when (item) {
				LoadMoreItem.LOADING -> {
					progressBar.visibility = View.VISIBLE
					seeOlderTextView.visibility = View.GONE
				}
				LoadMoreItem.NOT_FOUND -> {
					progressBar.visibility = View.GONE
					seeOlderTextView.visibility = View.GONE
				}
				LoadMoreItem.DEFAULT -> {
					progressBar.visibility = View.GONE
					seeOlderTextView.visibility = View.VISIBLE
				}
			}
		}
	}
}

