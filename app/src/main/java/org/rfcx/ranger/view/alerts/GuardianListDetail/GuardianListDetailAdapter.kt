package org.rfcx.ranger.view.alerts.GuardianListDetail

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
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

class GuardianListDetailAdapter(val listener: AlertClickListener) : ListAdapter<EventItem, GuardianListDetailAdapter.GuardianListDetailViewHolder>(GuardianListDetailDiffUtil()), KoinComponent {
	
	private val eventsDb: EventDb by inject()
	
	var stutasVisibility: ArrayList<Boolean> = arrayListOf()
	var currentEventList: MutableList<EventItem>? = null
	var updateList: Boolean = false
	
	
	var allItem: ArrayList<EventGroupByValue> = arrayListOf()
		set(value) {
			field = value
			notifyDataSetChanged()
		}
	
	var mOnSeeOlderClickListener: OnSeeOlderClickListener? = null
	
	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GuardianListDetailViewHolder {
		val view = LayoutInflater.from(parent.context).inflate(R.layout.item_guardian_list_detail, parent, false)
		return GuardianListDetailViewHolder(view)
	}
	
	override fun getItemCount(): Int = allItem.size
	
	override fun onBindViewHolder(holder: GuardianListDetailViewHolder, position: Int) {
		holder.bind(allItem[position].events, allItem[position].numberOfUnread(eventsDb), position)
	}
	
	class GuardianListDetailDiffUtil : DiffUtil.ItemCallback<EventItem>() {
		override fun areItemsTheSame(oldItem: EventItem, newItem: EventItem): Boolean {
			return oldItem.state == newItem.state
			
		}
		
		override fun areContentsTheSame(oldItem: EventItem, newItem: EventItem): Boolean {
			return oldItem.event.event_guid == newItem.event.event_guid
					&& oldItem.event.value == newItem.event.value
					&& oldItem.state == newItem.state
		}
	}
	
	fun handleShowDropDown(itemView: View, state: State) {
		val circleImageView = itemView.circleImageView
		val numOfEventsNotOpen = itemView.numOfEventsNotOpen
		val downChevronImageView = itemView.downChevronImageView
		val upChevronImageView = itemView.upChevronImageView
		
		when (state) {
			State.SHOW_UP -> {
				circleImageView.visibility = View.INVISIBLE
				numOfEventsNotOpen.visibility = View.INVISIBLE
				downChevronImageView.visibility = View.INVISIBLE
				upChevronImageView.visibility = View.VISIBLE
			}
			State.SHOW_DOWN -> {
				circleImageView.visibility = View.INVISIBLE
				numOfEventsNotOpen.visibility = View.INVISIBLE
				upChevronImageView.visibility = View.INVISIBLE
				downChevronImageView.visibility = View.VISIBLE
			}
			State.SHOW_NUM -> {
				circleImageView.visibility = View.VISIBLE
				numOfEventsNotOpen.visibility = View.VISIBLE
				upChevronImageView.visibility = View.INVISIBLE
				downChevronImageView.visibility = View.INVISIBLE
			}
		}
	}
	
	fun handleVisibilityList(itemView: View, state: Boolean, num: Int, position: Int) {
		val guardianListDetailGroupView = itemView.guardianListDetailGroupView
		
		if (state) {
			guardianListDetailGroupView.visibility = View.GONE
			if (num == 0) {
				handleShowDropDown(itemView, State.SHOW_DOWN)
			} else {
				handleShowDropDown(itemView, State.SHOW_NUM)
			}
			stutasVisibility[position] = false
			updateList = true
		} else {
			guardianListDetailGroupView.visibility = View.VISIBLE
			handleShowDropDown(itemView, State.SHOW_UP)
			stutasVisibility[position] = true
			updateList = true
		}
	}
	
	inner class GuardianListDetailViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
		private val groupByGuardianTextView = itemView.guardianListDetailTextView
		private val numOfEventsNotOpen = itemView.numOfEventsNotOpen
		private val guardianListDetailRecycler = itemView.guardianListDetailRecycler
		private val iconAlert = itemView.ivAlertIcon
		private val seeOlderTextView = itemView.seeOlderTextView
		
		@SuppressLint("DefaultLocale")
		fun bind(eventList: MutableList<EventItem>, num: Int, position: Int) {
			eventList[0].event.value?.toEventIcon()?.let { iconAlert.setImageResource(it) }
			numOfEventsNotOpen.text = num.toString()
			
			when {
				updateList -> {
					handleVisibilityList(itemView, !stutasVisibility[position], num, position)
				}
				!updateList -> {
					stutasVisibility.add(position, false)
					if(num == 0){
						handleShowDropDown(itemView, State.SHOW_DOWN)
					} else {
						handleShowDropDown(itemView, State.SHOW_NUM)
					}
				}
			}
			
			if (eventList[0].event.value !== null) {
				groupByGuardianTextView.text = eventList[0].event.value!!.capitalize()
			}
			guardianListDetailRecycler.apply {
				layoutManager = LinearLayoutManager(context)
				adapter = EventsInEventNameAdater(eventList, listener)
			}
			
			itemView.setOnClickListener {
				handleVisibilityList(itemView, stutasVisibility[position], num, position)
			}
			
			seeOlderTextView.setOnClickListener {
				val guid = eventList[eventList.size-1].event.guardianGUID ?: ""
				val value = eventList[eventList.size-1].event.value ?: ""
				val endAt = eventList[eventList.size-1].event.endAt
				mOnSeeOlderClickListener?.onSeeOlderClick(guid, value, endAt)
			}
			currentEventList = eventList
		}
	}
}

enum class State {
	SHOW_UP, SHOW_DOWN, SHOW_NUM
}