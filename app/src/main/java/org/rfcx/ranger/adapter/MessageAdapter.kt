package org.rfcx.ranger.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.rfcx.ranger.R
import org.rfcx.ranger.adapter.entity.*
import org.rfcx.ranger.adapter.view.*
import org.rfcx.ranger.entity.event.Event
import org.rfcx.ranger.entity.message.Message
import org.rfcx.ranger.util.DateHelper
import org.rfcx.ranger.util.RealmHelper
import java.util.*

class MessageAdapter(private val context: Context, private var onMessageItemClickListener: OnMessageItemClickListener,
                     private var headerProtocol: HeaderProtocol) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
	
	companion object {
		const val VIEW_TYPE_HEADER = 0
		const val VIEW_TYPE_EVENT = 1
		const val VIEW_TYPE_MESSAGE = 2
		const val VIEW_TYPE_TITLE = 3
		const val VIEW_TYPE_EMPTY = 4
	}
	
	private var items: MutableList<BaseItem> = ArrayList()
	private var headerInformation: HeaderInformation? = null
	
	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
		val inflater = LayoutInflater.from(parent.context)
		return when (viewType) {
			
			VIEW_TYPE_HEADER -> {
				val itemView = inflater.inflate(R.layout.header_profile, parent, false)
				ProfileViewHolder(itemView, headerProtocol)
			}
			
			VIEW_TYPE_EVENT -> {
				val itemView = inflater.inflate(R.layout.item_event, parent, false)
				EventItemViewHolder(itemView, onMessageItemClickListener)
			}
			
			VIEW_TYPE_MESSAGE -> {
				val itemView = inflater.inflate(R.layout.item_message, parent, false)
				MessageViewHolder(itemView, onMessageItemClickListener)
			}
			
			VIEW_TYPE_TITLE -> {
				val itemView = inflater.inflate(R.layout.item_title_holder, parent, false)
				TitleViewHolder(itemView)
			}

			VIEW_TYPE_EMPTY -> {
				val itemView = inflater.inflate(R.layout.item_empty_holder, parent, false)
				EmptyViewHolder(itemView)
			}
			
			else -> {
				throw Exception("Invalid viewType")
			}
		}
	}
	
	override fun getItemViewType(position: Int): Int {
		return if (position == 0) VIEW_TYPE_HEADER else {
			val itemPosition = position - 1
			val item = items[itemPosition]
			when (item) {
				is EventItem -> VIEW_TYPE_EVENT
				is MessageItem -> VIEW_TYPE_MESSAGE
				is TitleItem -> VIEW_TYPE_TITLE
				is EmptyItem -> VIEW_TYPE_EMPTY
				else -> {
					throw Exception("Invalid viewType")
				}
			}
		}
	}
	
	override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
		if (position == 0) {
			val info = headerInformation
			if (info != null) {
				(holder as ProfileViewHolder).bind(context, info.nickname, info.location)
			}
			return
		}
		
		val item = items[position - 1]
		
		when (holder.itemViewType) {
			VIEW_TYPE_MESSAGE -> {
				(holder as MessageViewHolder).bind((item as MessageItem).message)
			}
			
			VIEW_TYPE_EVENT -> {
				(holder as EventItemViewHolder).bind(context, (item as EventItem).event)
			}
			
			VIEW_TYPE_TITLE -> {
				(holder as TitleViewHolder).bind((item as TitleItem).title)
			}
		}
	}
	
	override fun getItemCount(): Int = items.size + 1
	
	fun getItemAt(position: Int): BaseItem? {
		if (position > items.size || position < 1) {
			return null
		}
		return (items[position - 1])
	}
	
	fun updateMessages(messages: List<Message>?, events: List<Event>?) {
		this.items.clear()

		val recentList = ArrayList<BaseItem>()
		val historyList = ArrayList<BaseItem>()

		messages?.let {
			for (message in messages) {
				val localMessage = RealmHelper.getInstance().findLocalMessage(message.guid)
				localMessage?.let {
					message.isOpened = localMessage.isOpened
				}
				if (message.isOpened) {
					historyList.add(MessageItem(message))
				} else {
					recentList.add(MessageItem(message))
				}
			}
		}

		events?.let {
			for (event in events) {
				val localEvent = RealmHelper.getInstance().findLocalEvent(event.event_guid)
				localEvent?.let {
					event.isOpened = localEvent.isOpened
				}
				if (event.isOpened) {
					historyList.add(EventItem(event))
				} else {
					recentList.add(EventItem(event))
				}
			}
		}

		recentList.sortWith(compareByDescending {
			when (it) {
				is MessageItem -> DateHelper.getDateTime(it.message.time)
				is EventItem -> DateHelper.getDateTime(it.event.beginsAt)
				else -> {
					0
				}
			}
		})

		historyList.sortWith(compareByDescending {
			when (it) {
				is MessageItem -> DateHelper.getDateTime(it.message.time)
				is EventItem -> DateHelper.getDateTime(it.event.beginsAt)
				else -> {
					0
				}
			}
		})

		if (recentList.isNotEmpty()) {
			this.items.add(TitleItem(context.getString(R.string.recent_title)))
			this.items.addAll(recentList)
		}

		if (historyList.isNotEmpty()) {
			this.items.add(TitleItem(context.getString(R.string.history_title)))
			this.items.addAll(historyList)
		}

		if (recentList.isNullOrEmpty() && historyList.isNullOrEmpty()) {
			this.items.add(EmptyItem())
		}

		notifyDataSetChanged()
	}

	fun updateHeader(nickname: String, location: String, isLocationTracking: Boolean) {
		this.headerInformation = HeaderInformation(nickname, location, isLocationTracking)
		notifyItemChanged(0)
	}
	
	data class HeaderInformation(val nickname: String, val location: String, val isLocationTracking: Boolean)
}

