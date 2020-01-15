package org.rfcx.ranger.view.alerts.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.inappmessaging.internal.injection.components.AppComponent
import kotlinx.android.synthetic.main.item_alert.view.*
import org.rfcx.ranger.R
import org.rfcx.ranger.adapter.entity.BaseItem
import org.rfcx.ranger.util.EventItem
import org.rfcx.ranger.util.toEventIcon
import org.rfcx.ranger.util.toTimeSinceStringAlternativeTimeAgo


class AlertsAdapter(val listener: AlertClickListener) : ListAdapter<BaseItem, RecyclerView.ViewHolder>(AlertsDiffUtil()) {
	
	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
		return if (viewType == ITEM_LOADING_VIEW) {
			LoadingViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_loading,
					parent, false))
		} else {
			val view = LayoutInflater.from(parent.context).inflate(R.layout.item_alert, parent, false)
			AlertViewHolder(view)
		}
	}
	
	override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
		val item = getItem(position)
		if (holder is AlertViewHolder) {
			holder.bind(item as EventItem)
			holder.itemView.setOnClickListener { listener.onClickedAlert(item.event) }
		}
	}
	
	override fun getItemViewType(position: Int): Int {
		return when (getItem(position)) {
			is LoadingItem -> ITEM_LOADING_VIEW
			else -> ITEM_EVENT_VIEW
		}
	}
	
	companion object {
		private const val ITEM_EVENT_VIEW = 1
		private const val ITEM_LOADING_VIEW = 2
	}
	
	class AlertsDiffUtil : DiffUtil.ItemCallback<BaseItem>() {
		override fun areItemsTheSame(oldItem: BaseItem, newItem: BaseItem): Boolean {
			return if (oldItem is EventItem && newItem is EventItem) {
				oldItem.event.id == newItem.event.id
			} else {
				false
			}
		}
		
		override fun areContentsTheSame(oldItem: BaseItem, newItem: BaseItem): Boolean {
			return if (oldItem is EventItem && newItem is EventItem) {
				oldItem.event.id == newItem.event.id
						&& oldItem.event.value == newItem.event.value
						&& oldItem.state == newItem.state
						&& oldItem.event.reviewCreated.compareTo(newItem.event.reviewCreated) == 0
			} else {
				false
			}
		}
	}
	
	inner class AlertViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
		private val tvTitle = itemView.tvAlertTitle
		private val iconAlert = itemView.ivAlertIcon
		private val tvTimeAgo = itemView.tvAlertTimeAgo
		private val tvTimeAgoAfterReview = itemView.tvAlertTimeAgoAfterReview
		private val ivStatusRead = itemView.ivStatusRead
		private val tvReviewed = itemView.reviewedTextView
		private val linearLayout = itemView.linearLayout
		private val tvNameReviewer = itemView.nameReviewerTextView
		private val tvAgreeValue = itemView.agreeTextView
		private val tvRejectValue = itemView.rejectTextView
		private val ivAgree = itemView.agreeImageView
		private val ivReject = itemView.rejectImageView
		private val context = itemView.context
		
		fun setUpAlert(drawable: Drawable, imageView: AppCompatImageView) {
			ivStatusRead.visibility = View.INVISIBLE
			tvNameReviewer.visibility = View.VISIBLE
			linearLayout.visibility = View.VISIBLE
			tvReviewed.visibility = View.VISIBLE
			tvTimeAgoAfterReview.visibility = View.VISIBLE
			tvTimeAgo.visibility = View.INVISIBLE
			
			imageView.background = context.getImage(R.drawable.bg_circle_green)
			imageView.setImageDrawable(drawable)
		}
		
		@SuppressLint("SetTextI18n", "DefaultLocale")
		fun bind(item: EventItem) {
			tvTitle.text = item.event.guardianName
			item.event.value.toEventIcon().let { iconAlert.setImageResource(it) }
			tvTimeAgo.text = " ${item.event.beginsAt.toTimeSinceStringAlternativeTimeAgo(context)}"
			tvTimeAgoAfterReview.text = " ${item.event.beginsAt.toTimeSinceStringAlternativeTimeAgo(context)}"
			tvAgreeValue.text = item.event.confirmedCount.toString()
			tvRejectValue.text = item.event.rejectedCount.toString()
			val count = item.event.confirmedCount + item.event.rejectedCount
			tvReviewed.text = context.getString(if (count > 0) R.string.last_reviewed_by else R.string.not_have_review)
			tvNameReviewer.visibility = if (count > 0) View.VISIBLE else View.INVISIBLE
			
			when (item.state) {
				EventItem.State.CONFIRM -> {
					context.getImage(R.drawable.ic_confirm_event_white)?.let { setUpAlert(it, ivAgree) }
				}
				EventItem.State.REJECT -> {
					context.getImage(R.drawable.ic_reject_event_white)?.let { setUpAlert(it, ivReject) }
				}
				EventItem.State.NONE -> {
					ivStatusRead.visibility = View.VISIBLE
					tvNameReviewer.visibility = View.INVISIBLE
					linearLayout.visibility = View.INVISIBLE
					tvReviewed.visibility = View.INVISIBLE
					tvTimeAgoAfterReview.visibility = View.INVISIBLE
					tvTimeAgo.visibility = View.VISIBLE
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

class LoadingItem : BaseItem