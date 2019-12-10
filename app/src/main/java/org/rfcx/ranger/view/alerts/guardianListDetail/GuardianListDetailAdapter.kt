package org.rfcx.ranger.view.alerts.guardianListDetail

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_guardian_list_detail.view.*
import org.koin.core.KoinComponent
import org.koin.core.inject
import org.rfcx.ranger.R
import org.rfcx.ranger.data.local.EventDb
import org.rfcx.ranger.util.EventItem
import org.rfcx.ranger.util.toEventIcon
import org.rfcx.ranger.view.alerts.adapter.AlertClickListener
import java.util.*

class GuardianListDetailAdapter(val listener: AlertClickListener) : ListAdapter<EventItem, GuardianListDetailAdapter.GuardianListDetailViewHolder>(GuardianListDetailDiffUtil()), KoinComponent {
	
	private val eventsDb: EventDb by inject()
	var allItem: ArrayList<EventGroupByValue> = arrayListOf()
		set(value) {
			field = value
			notifyDataSetChanged()
		}
	
	var mOnItemViewClickListener: OnItemViewClickListener? = null
	
	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GuardianListDetailViewHolder {
		val view = LayoutInflater.from(parent.context).inflate(R.layout.item_guardian_list_detail, parent, false)
		return GuardianListDetailViewHolder(view)
	}
	
	override fun getItemCount(): Int = allItem.size
	
	override fun onBindViewHolder(holder: GuardianListDetailViewHolder, position: Int) {
		val item = allItem[position]
		holder.bind(item.events, item.numberOfUnread(eventsDb))
	}
	
	class GuardianListDetailDiffUtil : DiffUtil.ItemCallback<EventItem>() {
		override fun areItemsTheSame(oldItem: EventItem, newItem: EventItem): Boolean {
			return oldItem.state == newItem.state
			
		}
		
		override fun areContentsTheSame(oldItem: EventItem, newItem: EventItem): Boolean {
			return oldItem.event.id == newItem.event.id
					&& oldItem.event.value == newItem.event.value
					&& oldItem.state == newItem.state
		}
	}
	
	inner class GuardianListDetailViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
		private val groupByGuardianTextView = itemView.guardianListDetailTextView
		private val numOfEventsNotOpen = itemView.numOfEventsNotOpen
		private val circleImageView = itemView.circleImageView
		private val iconAlert = itemView.ivAlertIcon
		
		@SuppressLint("DefaultLocale")
		fun bind(eventList: MutableList<EventItem>, num: Int) {
			eventList[0].event.value.toEventIcon().let { iconAlert.setImageResource(it) }
			if (num == 0) {
				numOfEventsNotOpen.visibility = View.INVISIBLE
				circleImageView.visibility = View.INVISIBLE
			} else {
				numOfEventsNotOpen.visibility = View.VISIBLE
				circleImageView.visibility = View.VISIBLE
				numOfEventsNotOpen.text = if (num > 999) {
					"999+"
				} else {
					num.toString()
				}
			}
			groupByGuardianTextView.text = eventList[0].event.label.capitalize()
			
			itemView.setOnClickListener {
				mOnItemViewClickListener?.onItemViewClick(eventList[0].event.value, eventList[0].event.guardianName)
			}
		}
	}
}