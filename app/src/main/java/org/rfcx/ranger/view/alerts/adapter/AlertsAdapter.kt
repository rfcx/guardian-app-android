package org.rfcx.ranger.view.alerts.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_alert.view.*
import org.rfcx.ranger.R
import org.rfcx.ranger.adapter.entity.BaseItem
import org.rfcx.ranger.entity.event.Event
import org.rfcx.ranger.util.toEventIcon
import org.rfcx.ranger.util.toTimeSinceStringAlternative


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
				oldItem.state == newItem.state
			} else {
				false
			}
		}
		
		override fun areContentsTheSame(oldItem: BaseItem, newItem: BaseItem): Boolean {
			return if (oldItem is EventItem && newItem is EventItem) {
				oldItem.event.event_guid == newItem.event.event_guid
						&& oldItem.event.value == newItem.event.value
						&& oldItem.state == newItem.state
			} else {
				false
			}
		}
	}
	
	inner class AlertViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
		private val tvTitle = itemView.tvAlertTitle
		private val iconAlert = itemView.ivAlertIcon
		private val tvFrom = itemView.tvAlertFromSite
		private val tvTimeAgo = itemView.tvAlertTimeAgo
		private val ivStatusRead = itemView.ivStatusRead
		private val ivReviewed = itemView.ivReviewed
		
		@SuppressLint("SetTextI18n", "DefaultLocale")
		fun bind(item: EventItem) {
			tvTitle.text = item.event.guardianShortname
			item.event.value?.toEventIcon()?.let { iconAlert.setImageResource(it) }
			if (item.event.site != null) {
				tvFrom.text = item.event.site!!.capitalize()
			}
			tvTimeAgo.text = "• ${item.event.beginsAt.toTimeSinceStringAlternative(itemView.context)}"
			when (item.state) {
				EventItem.State.CONFIRM -> {
					ivReviewed.setImageResource(R.drawable.ic_check)
					ivReviewed.setBackgroundResource(R.drawable.circle_green_stroke)
					ivReviewed.visibility = View.VISIBLE
					ivStatusRead.visibility = View.INVISIBLE
				}
				EventItem.State.REJECT -> {
					ivReviewed.setImageResource(R.drawable.ic_wrong)
					ivReviewed.setBackgroundResource(R.drawable.circle_green_stroke)
					ivReviewed.visibility = View.VISIBLE
					ivStatusRead.visibility = View.INVISIBLE
				}
				EventItem.State.NONE -> {
					ivReviewed.visibility = View.INVISIBLE
					ivStatusRead.visibility = View.VISIBLE
				}
			}
		}
	}
}

class LoadingItem : BaseItem

data class EventItem(val event: Event, var state: State = State.NONE) : BaseItem {
	
	enum class State {
		CONFIRM, REJECT, NONE
	}
}